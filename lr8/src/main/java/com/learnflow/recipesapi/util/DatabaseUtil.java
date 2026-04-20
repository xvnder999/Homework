package com.learnflow.recipesapi.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public final class DatabaseUtil {

    private static final Logger LOGGER = Logger.getLogger(DatabaseUtil.class.getName());

    private static final String URL      = "jdbc:postgresql://localhost:5432/recipes_db";
    private static final String USER     = "leej";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.severe("PostgreSQL Driver не найден: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private DatabaseUtil() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
