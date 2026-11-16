package com.bank.dao;

import com.bank.model.Loan;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class LoanDao extends AbstractDao<Loan> {
    private static final String SELECT = "SELECT l.*, s.title AS status_title, a.account_no, a.client_id, "
            + "c.last_name || ' ' || c.first_name AS client_name, cur.code AS currency_code, "
            + "(SELECT COALESCE(SUM(o.debit_amount), 0) FROM operations o "
            + " WHERE o.loan_id = l.id AND o.type_code = 'LOAN_PAYMENT') AS paid_amount "
            + "FROM loans l "
            + "JOIN loan_statuses s ON s.code = l.status_code "
            + "JOIN accounts a ON a.id = l.account_id "
            + "JOIN clients c ON c.id = a.client_id "
            + "JOIN currencies cur ON cur.id = a.currency_id";

    @Override
    protected String selectSql() {
        return SELECT;
    }

    @Override
    protected String idColumn() {
        return "l.id";
    }

    @Override
    protected String tableName() {
        return "loans";
    }

    @Override
    protected String orderBy() {
        return "l.id DESC";
    }

    @Override
    protected Loan map(ResultSet row) throws SQLException {
        Loan loan = new Loan();
        loan.setId(row.getInt("id"));
        loan.setAccountId(row.getInt("account_id"));
        loan.setAmount(row.getBigDecimal("amount"));
        loan.setAnnualRate(row.getBigDecimal("annual_rate"));
        loan.setTermMonths(row.getInt("term_months"));
        loan.setAppliedOn(row.getDate("applied_on"));
        loan.setIssuedOn(row.getDate("issued_on"));
        loan.setStatusCode(row.getString("status_code"));
        loan.setStatusTitle(row.getString("status_title"));
        loan.setAccountNo(row.getString("account_no"));
        loan.setClientId(row.getInt("client_id"));
        loan.setClientName(row.getString("client_name"));
        loan.setCurrencyCode(row.getString("currency_code"));
        loan.setPaidAmount(row.getBigDecimal("paid_amount"));
        return loan;
    }

    @Override
    protected int insert(Loan l) {
        return executeInsert("INSERT INTO loans (account_id, amount, annual_rate, term_months, applied_on, status_code) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                l.getAccountId(), l.getAmount(), l.getAnnualRate(), l.getTermMonths(), l.getAppliedOn(),
                l.getStatusCode());
    }

    @Override
    protected void update(Loan l) {
        execute("UPDATE loans SET amount = ?, annual_rate = ?, term_months = ? WHERE id = ?",
                l.getAmount(), l.getAnnualRate(), l.getTermMonths(), l.getId());
    }

    public List<Loan> findByClient(int clientId) {
        return query(SELECT + " WHERE a.client_id = ? ORDER BY " + orderBy(), clientId);
    }

    public boolean hasActiveOnAccount(int accountId) {
        return queryOne(SELECT + " WHERE l.account_id = ? AND l.status_code IN ('ACTIVE', 'PENDING') "
                + "FETCH FIRST 1 ROWS ONLY", accountId) != null;
    }

    /** Смена статуса только из ожидаемого состояния защищает от повторного выполнения параллельными запросами. */
    public boolean changeStatus(int id, String fromStatus, String toStatus, Date issuedOn) {
        return execute("UPDATE loans SET status_code = ?, issued_on = COALESCE(?, issued_on) "
                + "WHERE id = ? AND status_code = ?", toStatus, issuedOn, id, fromStatus) == 1;
    }

    /** Блокирует строку кредита до конца транзакции, чтобы платежи проводились последовательно. */
    public void lock(int id) {
        execute("UPDATE loans SET term_months = term_months WHERE id = ?", id);
    }
}
