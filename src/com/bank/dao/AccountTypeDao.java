package com.bank.dao;

import com.bank.model.AccountType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountTypeDao extends AbstractDao<AccountType> {
    @Override
    protected String selectSql() {
        return "SELECT * FROM account_types";
    }

    @Override
    protected String idColumn() {
        return "id";
    }

    @Override
    protected String tableName() {
        return "account_types";
    }

    @Override
    protected String orderBy() {
        return "id";
    }

    @Override
    protected AccountType map(ResultSet row) throws SQLException {
        AccountType type = new AccountType();
        type.setId(row.getInt("id"));
        type.setName(row.getString("name"));
        type.setAnnualRate(row.getBigDecimal("annual_rate"));
        return type;
    }

    @Override
    protected int insert(AccountType t) {
        return executeInsert("INSERT INTO account_types (name, annual_rate) VALUES (?, ?)",
                t.getName(), t.getAnnualRate());
    }

    @Override
    protected void update(AccountType t) {
        execute("UPDATE account_types SET name = ?, annual_rate = ? WHERE id = ?",
                t.getName(), t.getAnnualRate(), t.getId());
    }
}
