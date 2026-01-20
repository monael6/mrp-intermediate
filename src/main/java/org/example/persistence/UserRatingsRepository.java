package org.example.persistence;

import org.example.db.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class UserRatingsRepository {

    public static ArrayList<HashMap<String, Object>> listUserRatings(int userId) throws Exception {
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     """
                     SELECT r.id, r.media_id, r.stars, r.comment, r.comment_confirmed,
                            r.created_at, r.updated_at,
                            m.title AS media_title
                     FROM ratings r
                     JOIN media m ON m.id = r.media_id
                     WHERE r.user_id = ?
                     ORDER BY r.created_at DESC
                     """
             )) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HashMap<String, Object> x = new HashMap<>();
                x.put("id", rs.getInt("id"));
                x.put("mediaId", rs.getInt("media_id"));
                x.put("mediaTitle", rs.getString("media_title"));
                x.put("stars", rs.getInt("stars"));
                x.put("comment", rs.getString("comment"));
                x.put("commentConfirmed", rs.getBoolean("comment_confirmed"));
                x.put("createdAt", rs.getTimestamp("created_at"));
                x.put("updatedAt", rs.getTimestamp("updated_at"));
                list.add(x);
            }
        }

        return list;
    }
}
