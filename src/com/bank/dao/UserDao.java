package com.bank.dao;

import com.bank.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao extends AbstractDao<User> {
    private static final String SELECT = "SELECT u.*, c.last_name || ' ' || c.first_name AS client_name "
            + "FROM users u LEFT JOIN clients c ON c.id = u.client_id";

    @Override
    protected String selectSql() {
        return SELECT;
    }

    @Override
    protected String idColumn() {
        return "u.id";
    }

    @Override
    protected String tableName() {
        return "users";
    }

    @Override
    protected String orderBy() {
        return "u.login";
    }

    @Override
    protected User map(ResultSet row) throws SQLException {
        User user = new User();
        user.setId(row.getInt("id"));
        user.setLogin(row.getString("login"));
        user.setPasswordHash(row.getString("password_hash"));
        user.setRoleCode(row.getString("role_code"));
        user.setClientId(nullableInt(row, "client_id"));
        user.setActive(row.getBoolean("active"));
        user.setClientName(row.getString("client_name"));
        return user;
    }

    @Override
    protected int insert(User u) {
        return executeInsert("INSERT INTO users (login, password_hash, role_code, client_id, active) "
                + "VALUES (?, ?, ?, ?, ?)", u.getLogin(), u.getPasswordHash(), u.getRoleCode(), u.getClientId(), u.isActive());
    }

    @Override
    protected void update(User u) {
        execute("UPDATE users SET login = ?, password_hash = ?, role_code = ?, client_id = ?, active = ? WHERE id = ?",
                u.getLogin(), u.getPasswordHash(), u.getRoleCode(), u.getClientId(), u.isActive(), u.getId());
    }

    public User findByLogin(String login) {
        return queryOne(SELECT + " WHERE u.login = ?", login);
    }

    public List<User> findActiveAdmins() {
        return query(SELECT + " WHERE u.role_code = 'ADMIN' AND u.active = TRUE");
    }

    public void updatePassword(int id, String hash) {
        execute("UPDATE users SET password_hash = ? WHERE id = ?", hash, id);
    }
}
