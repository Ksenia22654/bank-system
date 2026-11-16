package com.bank.report;

import com.bank.util.Money;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Текстовый отчёт в виде таблицы с выравниванием по столбцам (паттерн «Шаблонный метод»):
 * общий порядок формирования задан здесь, содержание определяют наследники.
 */
public abstract class TextReport {
    private static final String SEPARATOR = "  ";

    protected abstract String getTitle();

    protected abstract String[] getHeaders();

    protected abstract List<String[]> getRows();

    /** Базовое имя файла латиницей, без расширения. */
    public abstract String getFileBaseName();

    /** Итоговая строка под таблицей; по умолчанию — количество записей. */
    protected String getFooter() {
        return "Всего записей: " + getRows().size();
    }

    public final String render() {
        String[] headers = getHeaders();
        List<String[]> rows = getRows();
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        }
        StringBuilder out = new StringBuilder();
        out.append(getTitle()).append("\r\n");
        out.append("Сформирован: ").append(date(new Date(), "dd.MM.yyyy HH:mm")).append("\r\n\r\n");
        appendRow(out, headers, widths);
        int total = headers.length * SEPARATOR.length();
        for (int width : widths) {
            total += width;
        }
        out.append(repeat('-', total - SEPARATOR.length())).append("\r\n");
        for (String[] row : rows) {
            appendRow(out, row, widths);
        }
        out.append("\r\n").append(getFooter()).append("\r\n");
        return out.toString();
    }

    public String getFileName() {
        return getFileBaseName() + "-" + date(new Date(), "yyyyMMdd-HHmmss") + ".txt";
    }

    private static void appendRow(StringBuilder out, String[] cells, int[] widths) {
        for (int i = 0; i < cells.length; i++) {
            out.append(cells[i]);
            if (i < cells.length - 1) {
                out.append(repeat(' ', widths[i] - cells[i].length())).append(SEPARATOR);
            }
        }
        out.append("\r\n");
    }

    private static String repeat(char c, int count) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < count; i++) {
            s.append(c);
        }
        return s.toString();
    }

    protected static String text(String value) {
        return value == null ? "—" : value.trim();
    }

    protected static String money(BigDecimal value) {
        return Money.format(value).replace(' ', ' ');
    }

    protected static String money(BigDecimal value, String currency) {
        return Money.format(value, currency == null ? "" : currency).replace(' ', ' ').trim();
    }

    protected static String date(Date value) {
        return value == null ? "—" : date(value, "dd.MM.yyyy");
    }

    protected static String date(Date value, String pattern) {
        return new SimpleDateFormat(pattern).format(value);
    }
}
