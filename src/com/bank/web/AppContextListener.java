package com.bank.web;

import com.bank.dao.Database;
import com.bank.dao.SqlScriptRunner;
import com.bank.util.AppConfig;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/** При запуске читает настройки и создаёт базу из SQL-скрипта, при остановке корректно закрывает СУБД. */
@WebListener
public class AppContextListener implements ServletContextListener {
    private static final Logger LOG = Logger.getLogger(AppContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            AppConfig.load();
            File dataDir = AppConfig.dataDir();
            dataDir.mkdirs();
            System.setProperty("derby.stream.error.file", new File(dataDir, "derby.log").getPath());
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            Database.getInstance().configure(AppConfig.databaseUrl());
            try (Connection connection = Database.getInstance().open()) {
                if (!SqlScriptRunner.schemaExists(connection)) {
                    try (InputStream script = event.getServletContext().getResourceAsStream("/WEB-INF/sql/create_db.sql")) {
                        SqlScriptRunner.run(connection, script);
                    }
                    LOG.info("База данных создана из скрипта create_db.sql");
                }
            }
        } catch (IOException | SQLException | ClassNotFoundException e) {
            throw new IllegalStateException("Не удалось подготовить базу данных: " + e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true").close();
        } catch (SQLException e) {
            // Штатное завершение Derby всегда сообщается исключением с кодом XJ015.
            if (!"XJ015".equals(e.getSQLState())) {
                LOG.log(Level.WARNING, "Ошибка при остановке СУБД", e);
            }
        }
        for (Driver driver : Collections.list(DriverManager.getDrivers())) {
            if (driver.getClass().getClassLoader() == getClass().getClassLoader()) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException e) {
                    LOG.log(Level.WARNING, "Не удалось снять регистрацию драйвера", e);
                }
            }
        }
    }
}
