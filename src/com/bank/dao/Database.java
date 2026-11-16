package com.bank.dao;

import com.bank.exception.BankException;
import com.bank.exception.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Одиночка (Singleton), выдающий соединения JDBC. Транзакция привязывается к текущему потоку,
 * поэтому параллельные запросы сервера работают каждый со своим соединением.
 */
public final class Database {
    private static final Database INSTANCE = new Database();
    private static final int MAX_ATTEMPTS = 5;

    private final ThreadLocal<Connection> transaction = new ThreadLocal<>();
    private volatile String url;

    private Database() {
    }

    public static Database getInstance() {
        return INSTANCE;
    }

    public void configure(String databaseUrl) {
        this.url = databaseUrl;
    }

    public interface ConnectionWork<T> {
        T run(Connection connection) throws SQLException;
    }

    public interface TransactionWork<T> {
        T run() throws BankException;
    }

    public Connection open() throws SQLException {
        return DriverManager.getConnection(url);
    }

    /** Выполняет работу в текущей транзакции потока или на отдельном соединении. */
    public <T> T withConnection(ConnectionWork<T> work) {
        try {
            Connection current = transaction.get();
            if (current != null) {
                return work.run(current);
            }
            try (Connection connection = open()) {
                return work.run(connection);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка обращения к базе данных: " + e.getMessage(), e);
        }
    }

    /** Выполняет работу атомарно. При взаимоблокировке транзакция откатывается и повторяется. */
    public <T> T inTransaction(TransactionWork<T> work) throws BankException {
        if (transaction.get() != null) {
            return work.run();
        }
        for (int attempt = 1; ; attempt++) {
            try {
                return runOnce(work);
            } catch (DataAccessException e) {
                if (!e.isRetryable() || attempt == MAX_ATTEMPTS) {
                    throw e;
                }
            }
        }
    }

    private <T> T runOnce(TransactionWork<T> work) throws BankException {
        try (Connection connection = open()) {
            connection.setAutoCommit(false);
            transaction.set(connection);
            try {
                T result = work.run();
                connection.commit();
                return result;
            } catch (BankException | RuntimeException | SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                transaction.remove();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка транзакции: " + e.getMessage(), e);
        }
    }
}
