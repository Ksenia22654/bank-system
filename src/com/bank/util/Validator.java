package com.bank.util;

import com.bank.exception.ValidationException;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/** Серверная проверка данных. Накапливает ошибки по полям; первая ошибка поля сохраняется. */
public class Validator {
    private static final BigDecimal MAX_MONEY = new BigDecimal("9999999999999.99");

    private final Map<String, String> errors = new LinkedHashMap<>();

    public Validator required(String field, String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            add(field, "Заполните поле «" + label + "»");
        }
        return this;
    }

    public Validator required(String field, Object value, String label) {
        if (value == null) {
            add(field, "Заполните поле «" + label + "»");
        }
        return this;
    }

    public Validator maxLength(String field, String value, int max, String label) {
        if (value != null && value.length() > max) {
            add(field, "Поле «" + label + "» не длиннее " + max + " символов");
        }
        return this;
    }

    public Validator pattern(String field, String value, String regex, String message) {
        if (value != null && !value.isEmpty() && !Pattern.matches(regex, value)) {
            add(field, message);
        }
        return this;
    }

    /** Сумма больше нуля, не более двух знаков после запятой. */
    public Validator money(String field, BigDecimal value, String label) {
        if (value == null) {
            add(field, "Укажите корректное значение поля «" + label + "»");
        } else if (value.signum() <= 0) {
            add(field, "Поле «" + label + "» должно быть больше нуля");
        } else if (value.scale() > 2 && value.stripTrailingZeros().scale() > 2) {
            add(field, "Поле «" + label + "» содержит более двух знаков после запятой");
        } else if (value.compareTo(MAX_MONEY) > 0) {
            add(field, "Поле «" + label + "» слишком велико");
        }
        return this;
    }

    public Validator range(String field, BigDecimal value, BigDecimal min, BigDecimal max, String label) {
        if (value == null) {
            add(field, "Укажите корректное значение поля «" + label + "»");
        } else if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            add(field, "Поле «" + label + "»: допустимо от " + min.toPlainString() + " до " + max.toPlainString());
        }
        return this;
    }

    public Validator range(String field, Integer value, int min, int max, String label) {
        if (value == null) {
            add(field, "Укажите корректное значение поля «" + label + "»");
        } else if (value < min || value > max) {
            add(field, "Поле «" + label + "»: допустимо от " + min + " до " + max);
        }
        return this;
    }

    public Validator notFuture(String field, Date value, String label) {
        if (value != null && value.after(new Date(System.currentTimeMillis()))) {
            add(field, "Поле «" + label + "» не может быть в будущем");
        }
        return this;
    }

    public Validator add(String field, String message) {
        errors.putIfAbsent(field, message);
        return this;
    }

    public void throwIfInvalid() throws ValidationException {
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
