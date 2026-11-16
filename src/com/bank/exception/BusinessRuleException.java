package com.bank.exception;

/** Нарушено правило предметной области, например на счёте недостаточно средств. */
public class BusinessRuleException extends BankException {
    private static final long serialVersionUID = 1L;

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
