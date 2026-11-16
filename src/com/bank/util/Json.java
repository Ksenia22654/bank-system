package com.bank.util;

import java.util.Iterator;
import java.util.Map;

/** Минимальная сериализация карт, списков, чисел и строк в JSON. */
public final class Json {
    private Json() {
    }

    public static String write(Object value) {
        StringBuilder out = new StringBuilder();
        append(out, value);
        return out.toString();
    }

    private static void append(StringBuilder out, Object value) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof Number || value instanceof Boolean) {
            out.append(value);
        } else if (value instanceof Map) {
            out.append('{');
            Iterator<? extends Map.Entry<?, ?>> entries = ((Map<?, ?>) value).entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<?, ?> entry = entries.next();
                appendString(out, String.valueOf(entry.getKey()));
                out.append(':');
                append(out, entry.getValue());
                if (entries.hasNext()) {
                    out.append(',');
                }
            }
            out.append('}');
        } else if (value instanceof Iterable) {
            out.append('[');
            Iterator<?> items = ((Iterable<?>) value).iterator();
            while (items.hasNext()) {
                append(out, items.next());
                if (items.hasNext()) {
                    out.append(',');
                }
            }
            out.append(']');
        } else {
            appendString(out, value.toString());
        }
    }

    private static void appendString(StringBuilder out, String text) {
        out.append('"');
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"':
                    out.append("\\\"");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                default:
                    if (c < 0x20 || c == '<' || c == '>') {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        out.append('"');
    }
}
