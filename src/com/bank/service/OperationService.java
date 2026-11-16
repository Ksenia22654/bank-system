package com.bank.service;

import com.bank.dao.AccountDao;
import com.bank.dao.Database;
import com.bank.dao.OperationDao;
import com.bank.exception.BankException;
import com.bank.exception.BusinessRuleException;
import com.bank.exception.NotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Account;
import com.bank.model.Operation;
import com.bank.model.User;
import com.bank.util.Validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

/** Проведение банковских операций. Каждая операция выполняется в одной транзакции. */
public class OperationService {
    private final Database database = Database.getInstance();
    private final AccountDao accounts = new AccountDao();
    private final OperationDao operations = new OperationDao();

    public List<Operation> listFor(User user, Date from, Date to) {
        return operations.search(user.isAdmin() ? null : user.getClientId(), from, to);
    }

    public List<Operation> recentFor(User user, int limit) {
        return user.isAdmin() ? operations.findRecent(limit) : operations.findRecent(user.getClientId(), limit);
    }

    public List<Operation> forAccount(int accountId) {
        return operations.findByAccount(accountId);
    }

    public void deposit(User actor, Integer accountId, BigDecimal amount, String description) throws BankException {
        Access.requireAdmin(actor);
        validate(accountId, amount, description);
        database.inTransaction(() -> {
            Account account = require(accountId);
            if (!accounts.credit(accountId, amount)) {
                throw new BusinessRuleException("Счёт " + account.getAccountNo() + " закрыт");
            }
            record(Operation.DEPOSIT, null, accountId, null, amount, null, actor, descriptionOr(description, "Пополнение наличными"));
            return null;
        });
    }

    public void withdraw(User actor, Integer accountId, BigDecimal amount, String description) throws BankException {
        Access.requireAdmin(actor);
        validate(accountId, amount, description);
        database.inTransaction(() -> {
            require(accountId);
            debitOrFail(accountId, amount);
            record(Operation.WITHDRAW, accountId, null, amount, null, null, actor, descriptionOr(description, "Снятие наличных"));
            return null;
        });
    }

    /**
     * Перевод между счетами с конвертацией по курсу. Строки счетов обновляются в порядке
     * возрастания идентификаторов, чтобы встречные параллельные переводы не приводили к взаимоблокировке.
     */
    public void transfer(User actor, Integer fromId, String toNumber, BigDecimal amount, String description)
            throws BankException {
        Validator v = new Validator();
        v.required("fromId", fromId, "Счёт списания");
        v.required("toNumber", toNumber, "Счёт зачисления");
        v.pattern("toNumber", toNumber, "[0-9]{20}", "Номер счёта должен состоять из 20 цифр");
        v.money("amount", amount, "Сумма");
        v.maxLength("description", description, 200, "Назначение");
        v.throwIfInvalid();

        database.inTransaction(() -> {
            Account from = require(fromId);
            Access.requireOwner(actor, from.getClientId());
            Account to = accounts.findByNumber(toNumber);
            if (to == null) {
                throw new ValidationException("toNumber", "Счёт зачисления не найден");
            }
            if (to.getId() == from.getId()) {
                throw new ValidationException("toNumber", "Счета списания и зачисления совпадают");
            }
            BigDecimal credited = convert(amount, from.getCurrencyRate(), to.getCurrencyRate());
            if (credited.signum() <= 0) {
                throw new ValidationException("amount", "Сумма слишком мала для конвертации");
            }
            if (from.getId() < to.getId()) {
                debitOrFail(from.getId(), amount);
                creditOrFail(to, credited);
            } else {
                creditOrFail(to, credited);
                debitOrFail(from.getId(), amount);
            }
            record(Operation.TRANSFER, from.getId(), to.getId(), amount, credited, null, actor,
                    descriptionOr(description, "Перевод средств"));
            return null;
        });
    }

    /** Начисляет проценты за месяц: остаток * ставка / 100 / 12. Повторно в том же месяце не начисляет. */
    public int accrueInterest(User actor) throws BankException {
        Access.requireAdmin(actor);
        Timestamp monthStart = Timestamp.valueOf(LocalDate.now().withDayOfMonth(1).atStartOfDay());
        return database.inTransaction(() -> {
            int count = 0;
            for (Account account : accounts.findActiveWithInterest()) {
                if (operations.hasInterestSince(account.getId(), monthStart)) {
                    continue;
                }
                BigDecimal interest = account.getBalance().multiply(account.getAnnualRate())
                        .divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);
                if (interest.signum() > 0 && accounts.credit(account.getId(), interest)) {
                    record(Operation.INTEREST, null, account.getId(), null, interest, null, actor,
                            "Проценты за месяц по ставке " + account.getAnnualRate() + "%");
                    count++;
                }
            }
            return count;
        });
    }

    /** Пересчёт суммы через базовую валюту с округлением до копеек. */
    public static BigDecimal convert(BigDecimal amount, BigDecimal fromRate, BigDecimal toRate) {
        return amount.multiply(fromRate).divide(toRate, 2, RoundingMode.HALF_UP);
    }

    void record(String type, Integer from, Integer to, BigDecimal debit, BigDecimal credit, Integer loanId,
                User actor, String description) {
        Operation op = new Operation();
        op.setTypeCode(type);
        op.setFromAccountId(from);
        op.setToAccountId(to);
        op.setDebitAmount(debit);
        op.setCreditAmount(credit);
        op.setLoanId(loanId);
        op.setPerformedAt(new Timestamp(System.currentTimeMillis()));
        op.setPerformedBy(actor.getId());
        op.setDescription(description);
        operations.save(op);
    }

    void debitOrFail(int accountId, BigDecimal amount) throws BusinessRuleException {
        if (!accounts.debit(accountId, amount)) {
            throw new BusinessRuleException("Недостаточно средств на счёте или счёт закрыт");
        }
    }

    private void creditOrFail(Account account, BigDecimal amount) throws BusinessRuleException {
        if (!accounts.credit(account.getId(), amount)) {
            throw new BusinessRuleException("Счёт зачисления закрыт");
        }
    }

    private Account require(int id) throws NotFoundException {
        Account account = accounts.findById(id);
        if (account == null) {
            throw new NotFoundException("Счёт не найден");
        }
        return account;
    }

    private static void validate(Integer accountId, BigDecimal amount, String description) throws ValidationException {
        new Validator().required("accountId", accountId, "Счёт")
                .money("amount", amount, "Сумма")
                .maxLength("description", description, 200, "Назначение")
                .throwIfInvalid();
    }

    private static String descriptionOr(String description, String fallback) {
        return description == null || description.trim().isEmpty() ? fallback : description.trim();
    }
}
