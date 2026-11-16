package com.bank.service;

import com.bank.dao.AccountDao;
import com.bank.dao.Database;
import com.bank.dao.LoanDao;
import com.bank.exception.BankException;
import com.bank.exception.BusinessRuleException;
import com.bank.exception.NotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Account;
import com.bank.model.Loan;
import com.bank.model.Operation;
import com.bank.model.User;
import com.bank.util.AppConfig;
import com.bank.util.Validator;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public class LoanService {
    private final Database database = Database.getInstance();
    private final LoanDao loans = new LoanDao();
    private final AccountDao accounts = new AccountDao();
    private final OperationService operations = new OperationService();

    public List<Loan> listFor(User user) {
        return user.isAdmin() ? loans.findAll() : loans.findByClient(user.getClientId());
    }

    public List<Loan> listByClient(int clientId) {
        return loans.findByClient(clientId);
    }

    public Loan get(User user, int id) throws BankException {
        Loan loan = loans.findById(id);
        if (loan == null) {
            throw new NotFoundException("Кредит не найден");
        }
        Access.requireOwner(user, loan.getClientId());
        return loan;
    }

    /** Заявка на кредит: ставка берётся из настроек, администратор может изменить её до одобрения. */
    public Loan apply(User user, Integer accountId, BigDecimal amount, Integer termMonths) throws BankException {
        Validator v = new Validator();
        v.required("accountId", accountId, "Счёт");
        v.money("amount", amount, "Сумма");
        v.range("termMonths", termMonths, 1, 360, "Срок");
        v.throwIfInvalid();
        Account account = accounts.findById(accountId);
        if (account == null) {
            throw new NotFoundException("Счёт не найден");
        }
        Access.requireOwner(user, account.getClientId());
        if (!account.isActive()) {
            throw new ValidationException("accountId", "Счёт закрыт");
        }
        Loan loan = new Loan();
        loan.setAccountId(accountId);
        loan.setAmount(amount);
        loan.setAnnualRate(AppConfig.defaultLoanRate());
        loan.setTermMonths(termMonths);
        loan.setAppliedOn(new Date(System.currentTimeMillis()));
        loan.setStatusCode(Loan.PENDING);
        return loans.save(loan);
    }

    /** Редактировать можно только заявку, которая ещё не рассмотрена. */
    public void update(int id, BigDecimal amount, BigDecimal annualRate, Integer termMonths) throws BankException {
        Validator v = new Validator();
        v.money("amount", amount, "Сумма");
        v.range("annualRate", annualRate, BigDecimal.ZERO, new BigDecimal("100"), "Ставка");
        v.range("termMonths", termMonths, 1, 360, "Срок");
        v.throwIfInvalid();
        Loan loan = loans.findById(id);
        if (loan == null) {
            throw new NotFoundException("Кредит не найден");
        }
        if (!loan.hasStatus(Loan.PENDING)) {
            throw new BusinessRuleException("Изменить можно только заявку на рассмотрении");
        }
        loan.setAmount(amount);
        loan.setAnnualRate(annualRate);
        loan.setTermMonths(termMonths);
        loans.save(loan);
    }

    /** Одобрение заявки: статус меняется, сумма кредита зачисляется на счёт клиента. */
    public void approve(User actor, int id) throws BankException {
        Access.requireAdmin(actor);
        database.inTransaction(() -> {
            Loan loan = get(actor, id);
            Date today = new Date(System.currentTimeMillis());
            if (!loans.changeStatus(id, Loan.PENDING, Loan.ACTIVE, today)) {
                throw new BusinessRuleException("Заявка уже рассмотрена");
            }
            if (!accounts.credit(loan.getAccountId(), loan.getAmount())) {
                throw new BusinessRuleException("Счёт клиента закрыт, кредит выдать нельзя");
            }
            operations.record(Operation.LOAN_ISSUE, null, loan.getAccountId(), null, loan.getAmount(), id, actor,
                    "Выдача кредита №" + id);
            return null;
        });
    }

    public void reject(User actor, int id) throws BankException {
        Access.requireAdmin(actor);
        if (!loans.changeStatus(id, Loan.PENDING, Loan.REJECTED, null)) {
            throw new BusinessRuleException("Заявка уже рассмотрена");
        }
    }

    /** Ежемесячный платёж: списание со счёта; когда долг выплачен полностью, кредит считается погашенным. */
    public void pay(User actor, int id) throws BankException {
        database.inTransaction(() -> {
            loans.lock(id);
            Loan loan = get(actor, id);
            if (!loan.hasStatus(Loan.ACTIVE)) {
                throw new BusinessRuleException("Платить можно только по действующему кредиту");
            }
            BigDecimal payment = loan.getMonthlyPayment().min(loan.getRemaining());
            operations.debitOrFail(loan.getAccountId(), payment);
            operations.record(Operation.LOAN_PAYMENT, loan.getAccountId(), null, payment, null, id, actor,
                    "Платёж по кредиту №" + id);
            if (loan.getRemaining().subtract(payment).signum() <= 0) {
                loans.changeStatus(id, Loan.ACTIVE, Loan.CLOSED, null);
            }
            return null;
        });
    }

    /** Удалить можно заявку, по которой не было выдачи. */
    public void delete(int id) throws BankException {
        Loan loan = loans.findById(id);
        if (loan == null) {
            throw new NotFoundException("Кредит не найден");
        }
        if (loan.hasStatus(Loan.ACTIVE) || loan.hasStatus(Loan.CLOSED)) {
            throw new BusinessRuleException("Выданный кредит удалить нельзя");
        }
        loans.delete(id);
    }
}
