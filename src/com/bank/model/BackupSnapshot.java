package com.bank.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/** Снимок всех изменяемых данных системы, который сериализуется в файл на сервере. */
public class BackupSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Timestamp createdAt;
    private final List<Currency> currencies;
    private final List<AccountType> accountTypes;
    private final List<Client> clients;
    private final List<User> users;
    private final List<Account> accounts;
    private final List<Loan> loans;
    private final List<Operation> operations;

    public BackupSnapshot(Timestamp createdAt, List<Currency> currencies, List<AccountType> accountTypes,
                          List<Client> clients, List<User> users, List<Account> accounts,
                          List<Loan> loans, List<Operation> operations) {
        this.createdAt = createdAt;
        this.currencies = currencies;
        this.accountTypes = accountTypes;
        this.clients = clients;
        this.users = users;
        this.accounts = accounts;
        this.loans = loans;
        this.operations = operations;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public List<Currency> getCurrencies() {
        return currencies;
    }

    public List<AccountType> getAccountTypes() {
        return accountTypes;
    }

    public List<Client> getClients() {
        return clients;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<Loan> getLoans() {
        return loans;
    }

    public List<Operation> getOperations() {
        return operations;
    }
}
