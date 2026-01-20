package org.example.persistence;

import org.example.db.Database;

import java.sql.*;
import java.util.HashMap;

public class UserProfileRepository {

    public static HashMap<String, Object> getProfile(int userId) throws Exception {
        HashMap<String, Object> map = new HashMap<>();

        try (Connection conn = Database.connect()) {

            // basic user
            PreparedStatement u = conn.prepareStatement(
                    "SELECT id, username, email, favorite_genre FROM users WHERE id = ?"
            );
            u.setInt(1, userId);
            ResultSet urs = u.executeQuery();
            if (!urs.next()) return null;

            map.put("id", urs.getInt("id"));
            map.put("username", urs.getString("username"));
            map.put("email", urs.getString("email"));
            map.put("favoriteGenre", urs.getString("favorite_genre"));

            // stats: ratingCount
            PreparedStatement r = conn.prepareStatement(
                    "SELECT COUNT(*) AS c FROM ratings WHERE user_id = ?"
            );
            r.setInt(1, userId);
            ResultSet rrs = r.executeQuery();
            rrs.next();
            map.put("ratingCount", rrs.getInt("c"));

            // stats: favoriteCount
            PreparedStatement f = conn.prepareStatement(
                    "SELECT COUNT(*) AS c FROM favorites WHERE user_id = ?"
            );
            f.setInt(1, userId);
            ResultSet frs = f.executeQuery();
            frs.next();
            map.put("favoriteCount", frs.getInt("c"));
        }

        return map;
    }

    public static void updateProfile(int userId, String email, String favoriteGenre) throws Exception {
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     """
                     UPDATE users
                     SET email = ?, favorite_genre = ?
                     WHERE id = ?
                     """
             )) {

            stmt.setString(1, email);
            stmt.setString(2, favoriteGenre);
            stmt.setInt(3, userId);

            stmt.executeUpdate();
        }
    }
}
