package com.bank.service;

import com.bank.dao.AnalyticsDao;
import com.bank.model.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Показатели и данные диаграмм. Суммы приведены к базовой валюте. */
public class AnalyticsService {
    private static final String[] MONTHS = {"янв", "фев", "мар", "апр", "май", "июн", "июл", "авг", "сен", "окт", "ноя", "дек"};
    private static final int TURNOVER_MONTHS = 6;

    private final AnalyticsDao dao = new AnalyticsDao();

    public Map<String, Object> summary(User user) {
        Map<String, Object> s = new LinkedHashMap<>();
        Integer clientId = user.isAdmin() ? null : user.getClientId();
        String owner = clientId != null ? " AND a.client_id = ?" : "";
        Object[] params = clientId != null ? new Object[]{clientId} : new Object[0];
        if (user.isAdmin()) {
            s.put("clients", dao.scalar("SELECT COUNT(*) FROM clients").intValue());
            s.put("pendingLoans", dao.scalar("SELECT COUNT(*) FROM loans WHERE status_code = 'PENDING'").intValue());
        }
        s.put("accounts", dao.scalar("SELECT COUNT(*) FROM accounts a WHERE a.closed_on IS NULL" + owner, params).intValue());
        s.put("activeLoans", dao.scalar("SELECT COUNT(*) FROM loans l JOIN accounts a ON a.id = l.account_id "
                + "WHERE l.status_code = 'ACTIVE'" + owner, params).intValue());
        s.put("totalBalance", dao.scalar("SELECT SUM(a.balance * c.rate) FROM accounts a "
                + "JOIN currencies c ON c.id = a.currency_id WHERE a.closed_on IS NULL" + owner, params)
                .setScale(2, RoundingMode.HALF_UP));
        return s;
    }

    public Map<String, Object> chartData(User user) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (user.isAdmin()) {
            data.put("balances", points(dao.pairs("SELECT c.code, SUM(a.balance * c.rate) FROM accounts a "
                    + "JOIN currencies c ON c.id = a.currency_id WHERE a.closed_on IS NULL "
                    + "GROUP BY c.code ORDER BY 2 DESC")));
            data.put("balancesTitle", "Остатки по валютам");
            data.put("types", points(dao.pairs("SELECT t.name, COUNT(*) * 1.0 FROM accounts a "
                    + "JOIN account_types t ON t.id = a.type_id WHERE a.closed_on IS NULL "
                    + "GROUP BY t.name ORDER BY 2 DESC")));
        } else {
            data.put("balances", points(dao.pairs("SELECT t.name || ' •' || SUBSTR(a.account_no, 17, 4), a.balance * c.rate "
                    + "FROM accounts a JOIN currencies c ON c.id = a.currency_id "
                    + "JOIN account_types t ON t.id = a.type_id "
                    + "WHERE a.closed_on IS NULL AND a.client_id = ? ORDER BY 2 DESC", user.getClientId())));
            data.put("balancesTitle", "Остатки по моим счетам");
        }
        data.put("turnover", turnover(user.isAdmin() ? null : user.getClientId()));
        return data;
    }

    private List<Map<String, Object>> turnover(Integer clientId) {
        LocalDate first = LocalDate.now().withDayOfMonth(1).minusMonths(TURNOVER_MONTHS - 1);
        Timestamp since = Timestamp.valueOf(first.atStartOfDay());
        Map<String, BigDecimal> incoming = dao.monthly(true, since, clientId);
        Map<String, BigDecimal> outgoing = dao.monthly(false, since, clientId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < TURNOVER_MONTHS; i++) {
            LocalDate month = first.plusMonths(i);
            String key = month.getYear() + "-" + month.getMonthValue();
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("label", MONTHS[month.getMonthValue() - 1]);
            point.put("incoming", incoming.getOrDefault(key, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
            point.put("outgoing", outgoing.getOrDefault(key, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
            result.add(point);
        }
        return result;
    }

    private static List<Map<String, Object>> points(Map<String, BigDecimal> pairs) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> pair : pairs.entrySet()) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("label", pair.getKey());
            point.put("value", pair.getValue().setScale(2, RoundingMode.HALF_UP));
            result.add(point);
        }
        return result;
    }
}
