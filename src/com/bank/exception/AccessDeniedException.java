package com.bank.exception;

/** У пользователя нет прав на выполнение действия. */
public class AccessDeniedException extends BankException {
    private static final long serialVersionUID = 1L;

    public AccessDeniedException(String message) {
        super(message);
    }
}
