package com.bank.dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

/** Агрегирующие запросы для аналитики. Суммы пересчитываются в базовую валюту. */
public class AnalyticsDao {
    private final Database database = Database.getInstance();

    public BigDecimal scalar(String sql, Object... params) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                AbstractDao.bind(statement, params);
                try (ResultSet row = statement.executeQuery()) {
                    row.next();
                    BigDecimal value = row.getBigDecimal(1);
                    return value == null ? BigDecimal.ZERO : value;
                }
            }
        });
    }

    /** Двухколоночная выборка «название — значение». */
    public Map<String, BigDecimal> pairs(String sql, Object... params) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                AbstractDao.bind(statement, params);
                try (ResultSet rows = statement.executeQuery()) {
                    Map<String, BigDecimal> result = new LinkedHashMap<>();
                    while (rows.next()) {
                        result.put(rows.getString(1).trim(), rows.getBigDecimal(2));
                    }
                    return result;
                }
            }
        });
    }

    /** Суммы зачислений (incoming = true) или списаний по месяцам: ключ — "ГГГГ-М". */
    public Map<String, BigDecimal> monthly(boolean incoming, Timestamp since, Integer clientId) {
        String accountColumn = incoming ? "o.to_account_id" : "o.from_account_id";
        String amountColumn = incoming ? "o.credit_amount" : "o.debit_amount";
        String sql = "SELECT y, m, SUM(v) FROM (SELECT YEAR(o.performed_at) AS y, MONTH(o.performed_at) AS m, "
                + amountColumn + " * cur.rate AS v FROM operations o "
                + "JOIN accounts a ON a.id = " + accountColumn + " "
                + "JOIN currencies cur ON cur.id = a.currency_id "
                + "WHERE o.performed_at >= ?" + (clientId != null ? " AND a.client_id = ?" : "")
                + ") t GROUP BY y, m";
        List<Object> params = new ArrayList<>();
        params.add(since);
        if (clientId != null) {
            params.add(clientId);
        }
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                AbstractDao.bind(statement, params.toArray());
                try (ResultSet rows = statement.executeQuery()) {
                    Map<String, BigDecimal> result = new LinkedHashMap<>();
                    while (rows.next()) {
                        result.put(rows.getInt(1) + "-" + rows.getInt(2), rows.getBigDecimal(3));
                    }
                    return result;
                }
            }
        });
    }
}
