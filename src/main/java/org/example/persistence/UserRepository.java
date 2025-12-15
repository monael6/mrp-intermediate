package org.example.persistence;

import org.example.db.Database;
import org.example.domain.User;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;


public class UserRepository {

    private static final Map<String, User> tokens = new HashMap<>();

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

    //Token speichern
public static void saveToken(String token, User user) {
    tokens.put(token, user);
}

    //User anhand Token holen
public static User getUserByToken(String token) {
    return tokens.get(token);
}
}