package com.bank.exception;

import java.sql.SQLException;

/** Непроверяемая обёртка над ошибками СУБД. */
public class DataAccessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, SQLException cause) {
        super(message, cause);
    }

    public String getSqlState() {
        return ((SQLException) getCause()).getSQLState();
    }

    public boolean isConstraintViolation() {
        String state = getSqlState();
        return state != null && state.startsWith("23");
    }

    /** Взаимоблокировка или истечение ожидания блокировки: транзакцию можно повторить. */
    public boolean isRetryable() {
        String state = getSqlState();
        return "40001".equals(state) || "40XL1".equals(state) || "40XL2".equals(state);
    }
}
