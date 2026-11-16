package com.bank.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Форматирование денежных сумм по русским правилам. */
public final class Money {
    private Money() {
    }

    public static String format(BigDecimal value) {
        if (value == null) {
            return "—";
        }
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.forLanguageTag("ru-RU"));
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(',');
        return new DecimalFormat("#,##0.00", symbols).format(value);
    }

    public static String format(BigDecimal value, String currencyCode) {
        return value == null ? "—" : format(value) + " " + currencyCode.trim();
    }
}
