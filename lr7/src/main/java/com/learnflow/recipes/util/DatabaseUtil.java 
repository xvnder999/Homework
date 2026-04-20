package com.learnflow.recipes.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public final class DatabaseUtil {

    private static final Logger LOGGER = Logger.getLogger(DatabaseUtil.class.getName());

    private static final String URL      = "jdbc:postgresql://localhost:5432/recipes_db";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "password";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.severe("PostgreSQL JDBC Driver не найден: " + e.getMessage());
            throw new RuntimeException("PostgreSQL Driver not found", e);
        }
    }

    private DatabaseUtil() {}

    /**
     * Возвращает новое соединение с базой данных.
     *
     * @return объект {@link Connection}
     * @throws SQLException при ошибке подключения
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
