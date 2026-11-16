package com.bank.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Создаёт базу данных из SQL-скрипта, если её таблицы ещё не существуют. */
public final class SqlScriptRunner {
    private SqlScriptRunner() {
    }

    public static boolean schemaExists(Connection connection) throws SQLException {
        try (ResultSet tables = connection.getMetaData().getTables(null, "APP", "CLIENTS", null)) {
            return tables.next();
        }
    }

    public static void run(Connection connection, InputStream script) throws IOException, SQLException {
        connection.setAutoCommit(false);
        try (Statement statement = connection.createStatement()) {
            for (String sql : split(script)) {
                statement.execute(sql);
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    /** Разбирает скрипт на команды: разделитель «;» вне строковых литералов, комментарии «--» отбрасываются. */
    static List<String> split(InputStream script) throws IOException {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(script, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!inString && line.trim().startsWith("--")) {
                    continue;
                }
                for (char c : line.toCharArray()) {
                    if (c == '\'') {
                        inString = !inString;
                    }
                    if (c == ';' && !inString) {
                        statements.add(current.toString().trim());
                        current.setLength(0);
                    } else {
                        current.append(c);
                    }
                }
                current.append('\n');
            }
        }
        if (!current.toString().trim().isEmpty()) {
            statements.add(current.toString().trim());
        }
        return statements;
    }
}
