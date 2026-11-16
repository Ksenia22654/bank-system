package com.bank.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

/** Настройки приложения: читаются из файла conf/bank.properties, код при этом не меняется. */
public final class AppConfig {
    private static final Properties PROPERTIES = new Properties();
    private static File home = new File(".");

    private AppConfig() {
    }

    public static synchronized void load() throws IOException {
        String homeProperty = System.getProperty("bank.home");
        home = homeProperty != null
                ? new File(homeProperty)
                : new File(System.getProperty("catalina.base", "."), "bank-data");
        File file = new File(home, "conf/bank.properties");
        if (file.isFile()) {
            try (InputStream in = Files.newInputStream(file.toPath());
                 InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                PROPERTIES.load(reader);
            }
        }
    }

    public static String get(String key, String defaultValue) {
        return PROPERTIES.getProperty(key, defaultValue);
    }

    public static BigDecimal getDecimal(String key, BigDecimal defaultValue) {
        String value = PROPERTIES.getProperty(key);
        return value == null ? defaultValue : new BigDecimal(value.trim());
    }

    public static File dataDir() {
        return new File(get("data.dir", new File(home, "data").getPath()));
    }

    public static File backupDir() {
        return new File(dataDir(), "backups");
    }

    public static String databaseUrl() {
        String path = new File(dataDir(), "bankdb").getPath().replace('\\', '/');
        return get("db.url", "jdbc:derby:" + path + ";create=true");
    }

    public static BigDecimal defaultLoanRate() {
        return getDecimal("loan.defaultRate", new BigDecimal("15.00"));
    }
}
