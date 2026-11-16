package com.bank.dao;

import com.bank.model.Account;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AccountDao extends AbstractDao<Account> {
    private static final String SELECT = "SELECT a.*, "
            + "c.last_name || ' ' || c.first_name AS client_name, "
            + "t.name AS type_name, t.annual_rate AS type_rate, cur.code AS currency_code, cur.rate AS currency_rate "
            + "FROM accounts a "
            + "JOIN clients c ON c.id = a.client_id "
            + "JOIN account_types t ON t.id = a.type_id "
            + "JOIN currencies cur ON cur.id = a.currency_id";

    @Override
    protected String selectSql() {
        return SELECT;
    }

    @Override
    protected String idColumn() {
        return "a.id";
    }

    @Override
    protected String tableName() {
        return "accounts";
    }

    @Override
    protected String orderBy() {
        return "a.id";
    }

    @Override
    protected Account map(ResultSet row) throws SQLException {
        Account account = new Account();
        account.setId(row.getInt("id"));
        account.setAccountNo(row.getString("account_no"));
        account.setClientId(row.getInt("client_id"));
        account.setTypeId(row.getInt("type_id"));
        account.setCurrencyId(row.getInt("currency_id"));
        account.setBalance(row.getBigDecimal("balance"));
        account.setOpenedOn(row.getDate("opened_on"));
        account.setClosedOn(row.getDate("closed_on"));
        account.setClientName(row.getString("client_name"));
        account.setTypeName(row.getString("type_name"));
        account.setAnnualRate(row.getBigDecimal("type_rate"));
        account.setCurrencyCode(row.getString("currency_code"));
        account.setCurrencyRate(row.getBigDecimal("currency_rate"));
        return account;
    }

    @Override
    protected int insert(Account a) {
        return executeInsert("INSERT INTO accounts (account_no, client_id, type_id, currency_id, balance, opened_on) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                a.getAccountNo(), a.getClientId(), a.getTypeId(), a.getCurrencyId(), a.getBalance(), a.getOpenedOn());
    }

    @Override
    protected void update(Account a) {
        execute("UPDATE accounts SET type_id = ? WHERE id = ?", a.getTypeId(), a.getId());
    }

    public Account findByNumber(String accountNo) {
        return queryOne(SELECT + " WHERE a.account_no = ?", accountNo);
    }

    public List<Account> findByClient(int clientId) {
        return query(SELECT + " WHERE a.client_id = ? ORDER BY a.id", clientId);
    }

    public List<Account> search(String text) {
        String like = "%" + text.toLowerCase() + "%";
        return query(SELECT + " WHERE a.account_no LIKE ? OR LOWER(c.last_name || ' ' || c.first_name) LIKE ? "
                + "ORDER BY a.id", like, like);
    }

    public List<Account> findActiveWithInterest() {
        return query(SELECT + " WHERE a.closed_on IS NULL AND a.balance > 0 AND t.annual_rate > 0 ORDER BY a.id");
    }

    /** Атомарное списание: возвращает false, если счёт закрыт или средств недостаточно. */
    public boolean debit(int accountId, BigDecimal amount) {
        return execute("UPDATE accounts SET balance = balance - ? "
                + "WHERE id = ? AND closed_on IS NULL AND balance >= ?", amount, accountId, amount) == 1;
    }

    /** Атомарное зачисление: возвращает false, если счёт закрыт. */
    public boolean credit(int accountId, BigDecimal amount) {
        return execute("UPDATE accounts SET balance = balance + ? "
                + "WHERE id = ? AND closed_on IS NULL", amount, accountId) == 1;
    }

    public boolean close(int accountId, Date date) {
        return execute("UPDATE accounts SET closed_on = ? "
                + "WHERE id = ? AND closed_on IS NULL AND balance = 0", date, accountId) == 1;
    }
}
