package org.example.persistence;

import org.example.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class LikeRepository {

    public static void like(int ratingId, int userId) throws Exception {
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement("""
                 INSERT INTO rating_likes (rating_id, user_id)
                 VALUES (?, ?)
                 ON CONFLICT DO NOTHING
             """)) {
            stmt.setInt(1, ratingId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public static void unlike(int ratingId, int userId) throws Exception {
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement("""
                 DELETE FROM rating_likes
                 WHERE rating_id = ? AND user_id = ?
             """)) {
            stmt.setInt(1, ratingId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }
}
