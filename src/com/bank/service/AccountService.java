package com.bank.service;

import com.bank.dao.AccountDao;
import com.bank.dao.ClientDao;
import com.bank.dao.CurrencyDao;
import com.bank.dao.Database;
import com.bank.dao.LoanDao;
import com.bank.exception.BankException;
import com.bank.exception.BusinessRuleException;
import com.bank.exception.NotFoundException;
import com.bank.model.Account;
import com.bank.model.Currency;
import com.bank.model.User;
import com.bank.util.Validator;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Date;
import java.util.List;

public class AccountService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Database database = Database.getInstance();
    private final AccountDao accounts = new AccountDao();
    private final ClientDao clients = new ClientDao();
    private final CurrencyDao currencies = new CurrencyDao();
    private final LoanDao loans = new LoanDao();

    public List<Account> listFor(User user, String text) {
        if (!user.isAdmin()) {
            return accounts.findByClient(user.getClientId());
        }
        return text == null || text.trim().isEmpty() ? accounts.findAll() : accounts.search(text.trim());
    }

    public List<Account> listByClient(int clientId) {
        return accounts.findByClient(clientId);
    }

    public Account get(User user, int id) throws BankException {
        Account account = accounts.findById(id);
        if (account == null) {
            throw new NotFoundException("Счёт не найден");
        }
        Access.requireOwner(user, account.getClientId());
        return account;
    }

    /** Открывает счёт. Пользователь открывает счёт только себе, администратор — любому клиенту. */
    public Account open(User user, Integer clientId, Integer typeId, Integer currencyId) throws BankException {
        new Validator().required("clientId", clientId, "Клиент")
                .required("typeId", typeId, "Вид счёта")
                .required("currencyId", currencyId, "Валюта")
                .throwIfInvalid();
        Access.requireOwner(user, clientId);
        if (clients.findById(clientId) == null) {
            throw new NotFoundException("Клиент не найден");
        }
        Currency currency = currencies.findById(currencyId);
        if (currency == null) {
            throw new NotFoundException("Валюта не найдена");
        }
        Account account = new Account();
        account.setClientId(clientId);
        account.setTypeId(typeId);
        account.setCurrencyId(currencyId);
        account.setOpenedOn(new Date(System.currentTimeMillis()));
        account.setBalance(BigDecimal.ZERO);
        do {
            account.setAccountNo(generateNumber(currency.getCode()));
        } while (accounts.findByNumber(account.getAccountNo()) != null);
        return accounts.save(account);
    }

    public void close(User user, int id) throws BankException {
        database.inTransaction(() -> {
            Account account = get(user, id);
            if (!account.isActive()) {
                throw new BusinessRuleException("Счёт уже закрыт");
            }
            if (loans.hasActiveOnAccount(id)) {
                throw new BusinessRuleException("К счёту привязан действующий кредит или заявка на кредит");
            }
            if (!accounts.close(id, new Date(System.currentTimeMillis()))) {
                throw new BusinessRuleException("Закрыть можно только счёт с нулевым остатком");
            }
            return null;
        });
    }

    public void delete(int id) throws BankException {
        Account account = accounts.findById(id);
        if (account == null) {
            throw new NotFoundException("Счёт не найден");
        }
        if (account.getBalance().signum() != 0) {
            throw new BusinessRuleException("Нельзя удалить счёт с ненулевым остатком");
        }
        accounts.delete(id);
    }

    /** Номер счёта: балансовый счёт 40817, цифровой код валюты и 12 случайных цифр. */
    static String generateNumber(String currencyCode) {
        String digital;
        switch (currencyCode.trim()) {
            case "RUB":
                digital = "810";
                break;
            case "USD":
                digital = "840";
                break;
            case "EUR":
                digital = "978";
                break;
            default:
                digital = "000";
        }
        StringBuilder number = new StringBuilder("40817").append(digital);
        for (int i = 0; i < 12; i++) {
            number.append(RANDOM.nextInt(10));
        }
        return number.toString();
    }
}
