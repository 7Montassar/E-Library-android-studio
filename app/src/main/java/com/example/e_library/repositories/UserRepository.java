package com.example.e_library.repositories;

import android.util.Log;

import com.example.e_library.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository extends BaseRepository {
    private static final String TAG = "UserRepository";

    public void save(User user) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, username, password, avatar, created_at) " +
                "VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            Log.d(TAG, "Attempting to save user: " + user.getUsername());

            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getUsername());
            pstmt.setString(4, user.getPassword());
            pstmt.setBytes(5, user.getAvatar());

            int rowsAffected = pstmt.executeUpdate();
            Log.d(TAG, "Rows affected: " + rowsAffected);

            if (rowsAffected == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error saving user: " + e.getMessage(), e);
            throw e;
        }
    }

    public boolean isEmailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    public boolean isUsernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                return user;
            }
            return null;
        }
    }
}