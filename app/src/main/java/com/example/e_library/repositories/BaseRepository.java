package com.example.e_library.repositories;

import com.example.e_library.database.DatabaseHelper;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class BaseRepository {
    protected Connection getConnection() throws SQLException {
        return DatabaseHelper.getConnection();
    }
}