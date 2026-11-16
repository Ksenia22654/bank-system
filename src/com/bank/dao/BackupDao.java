package com.bank.dao;

import com.bank.model.Account;
import com.bank.model.AccountType;
import com.bank.model.BackupSnapshot;
import com.bank.model.BaseEntity;
import com.bank.model.Client;
import com.bank.model.Currency;
import com.bank.model.Loan;
import com.bank.model.Operation;
import com.bank.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.function.Function;

/** Полная замена содержимого таблиц данными резервной копии. Вызывается внутри транзакции. */
public class BackupDao {
    private static final String[] TABLES = {"operations", "loans", "accounts", "users", "clients", "account_types", "currencies"};

    private final Database database = Database.getInstance();

    public void replaceAll(BackupSnapshot s) {
        database.withConnection(connection -> {
            try (Statement statement = connection.createStatement()) {
                for (String table : TABLES) {
                    statement.execute("LOCK TABLE " + table + " IN EXCLUSIVE MODE");
                }
                for (String table : TABLES) {
                    statement.executeUpdate("DELETE FROM " + table);
                }
            }
            insert(connection, "INSERT INTO currencies (id, code, name, rate) VALUES (?, ?, ?, ?)", s.getCurrencies(),
                    (Currency c) -> new Object[]{c.getId(), c.getCode(), c.getName(), c.getRate()});
            insert(connection, "INSERT INTO account_types (id, name, annual_rate) VALUES (?, ?, ?)", s.getAccountTypes(),
                    (AccountType t) -> new Object[]{t.getId(), t.getName(), t.getAnnualRate()});
            insert(connection, "INSERT INTO clients (id, last_name, first_name, middle_name, birth_date, passport_no, "
                            + "phone, email, address, registered_on) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", s.getClients(),
                    (Client c) -> new Object[]{c.getId(), c.getLastName(), c.getFirstName(), c.getMiddleName(), c.getBirthDate(),
                            c.getPassportNo(), c.getPhone(), c.getEmail(), c.getAddress(), c.getRegisteredOn()});
            insert(connection, "INSERT INTO users (id, login, password_hash, role_code, client_id, active) "
                            + "VALUES (?, ?, ?, ?, ?, ?)", s.getUsers(),
                    (User u) -> new Object[]{u.getId(), u.getLogin(), u.getPasswordHash(), u.getRoleCode(), u.getClientId(), u.isActive()});
            insert(connection, "INSERT INTO accounts (id, account_no, client_id, type_id, currency_id, balance, opened_on, closed_on) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", s.getAccounts(),
                    (Account a) -> new Object[]{a.getId(), a.getAccountNo(), a.getClientId(), a.getTypeId(), a.getCurrencyId(),
                            a.getBalance(), a.getOpenedOn(), a.getClosedOn()});
            insert(connection, "INSERT INTO loans (id, account_id, amount, annual_rate, term_months, applied_on, issued_on, status_code) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", s.getLoans(),
                    (Loan l) -> new Object[]{l.getId(), l.getAccountId(), l.getAmount(), l.getAnnualRate(), l.getTermMonths(),
                            l.getAppliedOn(), l.getIssuedOn(), l.getStatusCode()});
            insert(connection, "INSERT INTO operations (id, type_code, from_account_id, to_account_id, debit_amount, credit_amount, "
                            + "loan_id, performed_at, performed_by, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", s.getOperations(),
                    (Operation o) -> new Object[]{o.getId(), o.getTypeCode(), o.getFromAccountId(), o.getToAccountId(), o.getDebitAmount(),
                            o.getCreditAmount(), o.getLoanId(), o.getPerformedAt(), o.getPerformedBy(), o.getDescription()});
            restartIdentity(connection, "currencies", s.getCurrencies());
            restartIdentity(connection, "account_types", s.getAccountTypes());
            restartIdentity(connection, "clients", s.getClients());
            restartIdentity(connection, "users", s.getUsers());
            restartIdentity(connection, "accounts", s.getAccounts());
            restartIdentity(connection, "loans", s.getLoans());
            restartIdentity(connection, "operations", s.getOperations());
            return null;
        });
    }

    private static <T> void insert(Connection connection, String sql, List<T> rows, Function<T, Object[]> values)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (T row : rows) {
                AbstractDao.bind(statement, values.apply(row));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void restartIdentity(Connection connection, String table, List<? extends BaseEntity> rows)
            throws SQLException {
        int next = rows.stream().mapToInt(BaseEntity::getId).max().orElse(0) + 1;
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + table + " ALTER COLUMN id RESTART WITH " + next);
        }
    }
}
