package com.bank.exception;

/** Запрошенная запись не существует. */
public class NotFoundException extends BankException {
    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }
}
