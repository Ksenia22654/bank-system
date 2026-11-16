package com.bank.dao;

import com.bank.model.Operation;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class OperationDao extends AbstractDao<Operation> {
    private static final String SELECT = "SELECT o.*, t.title AS type_title, "
            + "fa.account_no AS from_no, ta.account_no AS to_no, "
            + "fc.code AS from_currency, tc.code AS to_currency, u.login AS performer "
            + "FROM operations o "
            + "JOIN operation_types t ON t.code = o.type_code "
            + "LEFT JOIN accounts fa ON fa.id = o.from_account_id "
            + "LEFT JOIN currencies fc ON fc.id = fa.currency_id "
            + "LEFT JOIN accounts ta ON ta.id = o.to_account_id "
            + "LEFT JOIN currencies tc ON tc.id = ta.currency_id "
            + "LEFT JOIN users u ON u.id = o.performed_by";
    private static final String NEWEST_FIRST = " ORDER BY o.performed_at DESC, o.id DESC";

    @Override
    protected String selectSql() {
        return SELECT;
    }

    @Override
    protected String idColumn() {
        return "o.id";
    }

    @Override
    protected String tableName() {
        return "operations";
    }

    @Override
    protected String orderBy() {
        return "o.id";
    }

    @Override
    protected Operation map(ResultSet row) throws SQLException {
        Operation op = new Operation();
        op.setId(row.getInt("id"));
        op.setTypeCode(row.getString("type_code"));
        op.setTypeTitle(row.getString("type_title"));
        op.setFromAccountId(nullableInt(row, "from_account_id"));
        op.setToAccountId(nullableInt(row, "to_account_id"));
        op.setDebitAmount(row.getBigDecimal("debit_amount"));
        op.setCreditAmount(row.getBigDecimal("credit_amount"));
        op.setLoanId(nullableInt(row, "loan_id"));
        op.setPerformedAt(row.getTimestamp("performed_at"));
        op.setPerformedBy(nullableInt(row, "performed_by"));
        op.setDescription(row.getString("description"));
        op.setFromAccountNo(row.getString("from_no"));
        op.setToAccountNo(row.getString("to_no"));
        op.setFromCurrency(row.getString("from_currency"));
        op.setToCurrency(row.getString("to_currency"));
        op.setPerformerLogin(row.getString("performer"));
        return op;
    }

    @Override
    protected int insert(Operation o) {
        return executeInsert("INSERT INTO operations (type_code, from_account_id, to_account_id, debit_amount, "
                        + "credit_amount, loan_id, performed_at, performed_by, description) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                o.getTypeCode(), o.getFromAccountId(), o.getToAccountId(), o.getDebitAmount(), o.getCreditAmount(),
                o.getLoanId(), o.getPerformedAt(), o.getPerformedBy(), o.getDescription());
    }

    @Override
    protected void update(Operation entity) {
        throw new UnsupportedOperationException("Проведённые операции не изменяются");
    }

    public List<Operation> findRecent(int limit) {
        return query(SELECT + NEWEST_FIRST + " FETCH FIRST " + limit + " ROWS ONLY");
    }

    public List<Operation> findByAccount(int accountId) {
        return query(SELECT + " WHERE o.from_account_id = ? OR o.to_account_id = ?" + NEWEST_FIRST,
                accountId, accountId);
    }

    public List<Operation> findRecent(int clientId, int limit) {
        return query(SELECT + " WHERE fa.client_id = ? OR ta.client_id = ?" + NEWEST_FIRST
                + " FETCH FIRST " + limit + " ROWS ONLY", clientId, clientId);
    }

    /** Поиск за период; clientId, from и to необязательны. */
    public List<Operation> search(Integer clientId, Date from, Date to) {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (clientId != null) {
            sql.append(" AND (fa.client_id = ? OR ta.client_id = ?)");
            params.add(clientId);
            params.add(clientId);
        }
        if (from != null) {
            sql.append(" AND o.performed_at >= ?");
            params.add(new Timestamp(from.getTime()));
        }
        if (to != null) {
            sql.append(" AND o.performed_at < ?");
            params.add(new Timestamp(to.getTime() + 24L * 60 * 60 * 1000));
        }
        return query(sql.append(NEWEST_FIRST).toString(), params.toArray());
    }

    public boolean hasInterestSince(int accountId, Timestamp since) {
        return queryOne(SELECT + " WHERE o.type_code = ? AND o.to_account_id = ? AND o.performed_at >= ? "
                + "FETCH FIRST 1 ROWS ONLY", Operation.INTEREST, accountId, since) != null;
    }
}
