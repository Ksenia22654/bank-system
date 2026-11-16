package com.bank.dao;

import com.bank.model.BaseEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Общая реализация DAO через JDBC. Наследники задают SQL и преобразование строки в объект. */
public abstract class AbstractDao<T extends BaseEntity> implements Dao<T> {
    protected final Database database = Database.getInstance();

    protected abstract String selectSql();

    protected abstract String idColumn();

    protected abstract String tableName();

    protected abstract String orderBy();

    protected abstract T map(ResultSet row) throws SQLException;

    protected abstract int insert(T entity);

    protected abstract void update(T entity);

    @Override
    public T findById(int id) {
        return queryOne(selectSql() + " WHERE " + idColumn() + " = ?", id);
    }

    @Override
    public List<T> findAll() {
        return query(selectSql() + " ORDER BY " + orderBy());
    }

    @Override
    public T save(T entity) {
        if (entity.isNew()) {
            entity.setId(insert(entity));
        } else {
            update(entity);
        }
        return entity;
    }

    @Override
    public boolean delete(int id) {
        return execute("DELETE FROM " + tableName() + " WHERE id = ?", id) > 0;
    }

    protected List<T> query(String sql, Object... params) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, params);
                try (ResultSet rows = statement.executeQuery()) {
                    List<T> result = new ArrayList<>();
                    while (rows.next()) {
                        result.add(map(rows));
                    }
                    return result;
                }
            }
        });
    }

    protected T queryOne(String sql, Object... params) {
        List<T> rows = query(sql, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    protected int execute(String sql, Object... params) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, params);
                return statement.executeUpdate();
            }
        });
    }

    /** Выполняет INSERT и возвращает сгенерированный идентификатор. */
    protected int executeInsert(String sql, Object... params) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bind(statement, params);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    keys.next();
                    return keys.getInt(1);
                }
            }
        });
    }

    protected static void bind(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) {
                statement.setNull(i + 1, statement.getParameterMetaData().getParameterType(i + 1));
            } else {
                statement.setObject(i + 1, params[i]);
            }
        }
    }

    protected static Integer nullableInt(ResultSet row, String column) throws SQLException {
        int value = row.getInt(column);
        return row.wasNull() ? null : value;
    }
}
