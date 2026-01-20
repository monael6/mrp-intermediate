package org.example.persistence;

import org.example.db.Database;

import java.sql.*;
import java.util.ArrayList;

public class LeaderboardRepository {

    public static ArrayList<String> topActiveUsersByRatings(int limit) throws Exception {
        ArrayList<String> list = new ArrayList<>();

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     """
                     SELECT u.id, u.username, COUNT(r.id) AS rating_count
                     FROM users u
                     LEFT JOIN ratings r ON r.user_id = u.id
                     GROUP BY u.id, u.username
                     ORDER BY rating_count DESC, u.username ASC
                     LIMIT ?
                     """
             )) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                int ratingCount = rs.getInt("rating_count");

                // simpel JSON pro Eintrag (ohne extra Domain-Klasse)
                String item = "{\"userId\":" + id + ",\"username\":\"" + escape(username) + "\",\"ratingCount\":" + ratingCount + "}";
                list.add(item);
            }
        }

        return list;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
