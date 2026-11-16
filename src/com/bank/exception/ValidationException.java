package com.bank.exception;

import java.util.LinkedHashMap;
import java.util.Map;

/** Ошибки проверки введённых данных: имя поля — текст ошибки. */
public class ValidationException extends BankException {
    private static final long serialVersionUID = 1L;

    private final LinkedHashMap<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super(String.join("; ", errors.values()));
        this.errors = new LinkedHashMap<>(errors);
    }

    public ValidationException(String field, String message) {
        super(message);
        this.errors = new LinkedHashMap<>();
        this.errors.put(field, message);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
