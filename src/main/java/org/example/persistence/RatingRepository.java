package org.example.persistence;

import org.example.db.Database;
import org.example.domain.Rating;

import java.sql.*;
import java.util.ArrayList;

public class RatingRepository {

    // Create OR Update (1 rating per user per media)
    // Wenn comment geändert wird -> comment_confirmed wieder FALSE (Moderation)
    public static void upsert(int mediaId, int userId, int stars, String comment) throws Exception {
        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement("""
                            INSERT INTO ratings (media_id, user_id, stars, comment, comment_confirmed, updated_at)
                            VALUES (?, ?, ?, ?, FALSE, CURRENT_TIMESTAMP)
                            ON CONFLICT (media_id, user_id)
                            DO UPDATE SET
                               stars = EXCLUDED.stars,
                               comment = EXCLUDED.comment,
                               comment_confirmed = FALSE,
                               updated_at = CURRENT_TIMESTAMP
                        """)) {

            stmt.setInt(1, mediaId);
            stmt.setInt(2, userId);
            stmt.setInt(3, stars);
            stmt.setString(4, comment);
            stmt.executeUpdate();
        }
    }

    public static Rating findById(int ratingId) throws Exception {
        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ratings WHERE id = ?")) {
            stmt.setInt(1, ratingId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next())
                return null;

            Rating r = new Rating();
            r.id = rs.getInt("id");
            r.mediaId = rs.getInt("media_id");
            r.userId = rs.getInt("user_id");
            r.stars = rs.getInt("stars");
            r.comment = rs.getString("comment");
            r.commentConfirmed = rs.getBoolean("comment_confirmed");
            return r;
        }
    }

    // PUBLIC list: comment nur wenn confirmed
    public static ArrayList<Rating> findByMediaIdPublic(int mediaId) throws Exception {
        ArrayList<Rating> list = new ArrayList<>();

        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement("""
                            SELECT r.id, r.media_id, r.user_id, r.stars,

                                   r.comment,
                                   r.comment_confirmed,
                                   COALESCE(l.cnt, 0) AS likes,
                                   r.created_at
                            FROM ratings r
                            LEFT JOIN (
                               SELECT rating_id, COUNT(*) AS cnt
                               FROM rating_likes
                               GROUP BY rating_id
                            ) l ON l.rating_id = r.id
                            WHERE r.media_id = ?
                            ORDER BY r.created_at DESC
                        """)) {

            stmt.setInt(1, mediaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Rating r = new Rating();
                r.id = rs.getInt("id");
                r.mediaId = rs.getInt("media_id");
                r.userId = rs.getInt("user_id");
                r.stars = rs.getInt("stars");
                r.comment = rs.getString("comment");
                r.commentConfirmed = rs.getBoolean("comment_confirmed");
                r.likes = rs.getInt("likes");
                r.createdAt = String.valueOf(rs.getTimestamp("created_at"));
                list.add(r);
            }
        }

        return list;
    }

    // Nur owner kann confirm
    public static boolean confirmComment(int ratingId, int requestingUserId) throws Exception {
        try (Connection conn = Database.connect()) {
            // Erst prüfen: Gehört das Media zu diesem User?
            try (PreparedStatement check = conn.prepareStatement("""
                        SELECT m.creator_id
                        FROM media m
                        JOIN ratings r ON r.media_id = m.id
                        WHERE r.id =?
                    """)) {
                check.setInt(1, ratingId);
                ResultSet rs = check.executeQuery();
                if (!rs.next())
                    return false; // Rating existiert nicht

                int creatorId = rs.getInt("creator_id");
                System.out.println("DEBUG: RatingID=" + ratingId + " belongs to Media created by User=" + creatorId
                        + ", RequestingUser=" + requestingUserId);
                if (creatorId != requestingUserId)
                    return false; // Nicht der Ersteller
            }

            // Dann Update
            try (PreparedStatement stmt = conn.prepareStatement("""
                        UPDATE ratings
                        SET comment_confirmed = TRUE
                        WHERE id = ? AND comment IS NOT NULL
                    """)) {
                stmt.setInt(1, ratingId);
                return stmt.executeUpdate() > 0;
            }
        }
    }

    public static boolean delete(int ratingId, int userId) throws Exception {
        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement("""
                            DELETE FROM ratings
                            WHERE id = ? AND user_id = ?
                        """)) {
            stmt.setInt(1, ratingId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Update per ratingId (spec: PUT /ratings/{ratingId})
    public static boolean updateById(int ratingId, int userId, int stars, String comment) throws Exception {
        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement("""
                            UPDATE ratings
                            SET stars = ?, comment = ?, comment_confirmed = FALSE, updated_at = CURRENT_TIMESTAMP
                            WHERE id = ? AND user_id = ?
                        """)) {

            stmt.setInt(1, stars);
            stmt.setString(2, comment);
            stmt.setInt(3, ratingId);
            stmt.setInt(4, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    // Optional helper (falls du es irgendwo brauchst)
    public static boolean exists(int ratingId) throws Exception {
        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM ratings WHERE id = ?")) {
            stmt.setInt(1, ratingId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
}
