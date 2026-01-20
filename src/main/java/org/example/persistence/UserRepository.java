package org.example.persistence;

import org.example.db.Database;
import org.example.domain.User;

import java.sql.*;

public class UserRepository {

    public static void save(User user) throws Exception {
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (username, password) VALUES (?, ?)"
             )) {
            stmt.setString(1, user.username);
            stmt.setString(2, user.password);
            stmt.executeUpdate();
        }
    }

    public static User findByUsername(String username) throws Exception {
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM users WHERE username = ?"
             )) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) return null;

            User u = new User();
            u.id = rs.getInt("id");
            u.username = rs.getString("username");
            u.password = rs.getString("password");
            return u;
        }
    }

    // Token speichern (DB persistent)
    public static void saveToken(String token, User user) throws Exception {
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO tokens (token, user_id) VALUES (?, ?) ON CONFLICT (token) DO NOTHING"
             )) {
            stmt.setString(1, token);
            stmt.setInt(2, user.id);
            stmt.executeUpdate();
        }
    }

    // User anhand Token holen (DB persistent)
    public static User getUserByToken(String token) throws Exception {
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement("""
                 SELECT u.id, u.username, u.password
                 FROM tokens t
                 JOIN users u ON u.id = t.user_id
                 WHERE t.token = ?
             """)) {

            stmt.setString(1, token.trim());
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) return null;

            User u = new User();
            u.id = rs.getInt("id");
            u.username = rs.getString("username");
            u.password = rs.getString("password");
            return u;
        }
    }

    // Optional: logout
    public static void deleteToken(String token) throws Exception {
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM tokens WHERE token = ?"
             )) {
            stmt.setString(1, token.trim());
            stmt.executeUpdate();
        }
    }
}
