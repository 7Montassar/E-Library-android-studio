package com.example.e_library.database;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DB_URL = "jdbc:postgresql://10.0.2.2:5432/elib_db";
    private static final String DB_USER = "elib_user";
    private static final String DB_PASSWORD = "elib_pass";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");

            Properties props = new Properties();
            props.setProperty("user", DB_USER);
            props.setProperty("password", DB_PASSWORD);

            Log.d(TAG, "Attempting to connect to database...");
            return DriverManager.getConnection(DB_URL, props);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "PostgreSQL JDBC Driver not found.", e);
            throw new SQLException("PostgreSQL JDBC Driver not found.");
        }
    }
}