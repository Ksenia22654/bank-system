package com.bank.dao;

import com.bank.model.Currency;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CurrencyDao extends AbstractDao<Currency> {
    @Override
    protected String selectSql() {
        return "SELECT * FROM currencies";
    }

    @Override
    protected String idColumn() {
        return "id";
    }

    @Override
    protected String tableName() {
        return "currencies";
    }

    @Override
    protected String orderBy() {
        return "id";
    }

    @Override
    protected Currency map(ResultSet row) throws SQLException {
        Currency currency = new Currency();
        currency.setId(row.getInt("id"));
        currency.setCode(row.getString("code"));
        currency.setName(row.getString("name"));
        currency.setRate(row.getBigDecimal("rate"));
        return currency;
    }

    @Override
    protected int insert(Currency c) {
        return executeInsert("INSERT INTO currencies (code, name, rate) VALUES (?, ?, ?)",
                c.getCode(), c.getName(), c.getRate());
    }

    @Override
    protected void update(Currency c) {
        execute("UPDATE currencies SET code = ?, name = ?, rate = ? WHERE id = ?",
                c.getCode(), c.getName(), c.getRate(), c.getId());
    }
}
