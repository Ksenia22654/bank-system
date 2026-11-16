package com.bank.dao;

import com.bank.model.Client;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ClientDao extends AbstractDao<Client> {
    @Override
    protected String selectSql() {
        return "SELECT * FROM clients";
    }

    @Override
    protected String idColumn() {
        return "id";
    }

    @Override
    protected String tableName() {
        return "clients";
    }

    @Override
    protected String orderBy() {
        return "last_name, first_name";
    }

    @Override
    protected Client map(ResultSet row) throws SQLException {
        Client client = new Client();
        client.setId(row.getInt("id"));
        client.setLastName(row.getString("last_name"));
        client.setFirstName(row.getString("first_name"));
        client.setMiddleName(row.getString("middle_name"));
        client.setBirthDate(row.getDate("birth_date"));
        client.setPassportNo(row.getString("passport_no"));
        client.setPhone(row.getString("phone"));
        client.setEmail(row.getString("email"));
        client.setAddress(row.getString("address"));
        client.setRegisteredOn(row.getDate("registered_on"));
        return client;
    }

    @Override
    protected int insert(Client c) {
        return executeInsert("INSERT INTO clients (last_name, first_name, middle_name, birth_date, passport_no, "
                        + "phone, email, address, registered_on) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                c.getLastName(), c.getFirstName(), c.getMiddleName(), c.getBirthDate(), c.getPassportNo(),
                c.getPhone(), c.getEmail(), c.getAddress(), c.getRegisteredOn());
    }

    @Override
    protected void update(Client c) {
        execute("UPDATE clients SET last_name = ?, first_name = ?, middle_name = ?, birth_date = ?, passport_no = ?, "
                        + "phone = ?, email = ?, address = ? WHERE id = ?",
                c.getLastName(), c.getFirstName(), c.getMiddleName(), c.getBirthDate(), c.getPassportNo(),
                c.getPhone(), c.getEmail(), c.getAddress(), c.getId());
    }

    public List<Client> search(String text) {
        String like = "%" + text.toLowerCase() + "%";
        return query(selectSql() + " WHERE LOWER(last_name || ' ' || first_name) LIKE ? "
                + "OR passport_no LIKE ? OR phone LIKE ? ORDER BY " + orderBy(), like, like, like);
    }

    public Client findByPassport(String passportNo) {
        return queryOne(selectSql() + " WHERE passport_no = ?", passportNo);
    }
}
