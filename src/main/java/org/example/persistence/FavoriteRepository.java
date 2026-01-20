package org.example.persistence;

import org.example.db.Database;
import org.example.domain.Media;

import java.sql.*;
import java.util.ArrayList;

public class FavoriteRepository {

    public static void add(int mediaId, int userId) throws Exception {
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement("""
                 INSERT INTO favorites (media_id, user_id)
                 VALUES (?, ?)
                 ON CONFLICT DO NOTHING
             """)) {
            stmt.setInt(1, mediaId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public static void remove(int mediaId, int userId) throws Exception {
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement("""
                 DELETE FROM favorites
                 WHERE media_id = ? AND user_id = ?
             """)) {
            stmt.setInt(1, mediaId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public static ArrayList<Media> listFavorites(int userId) throws Exception {
        ArrayList<Media> list = new ArrayList<>();

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement("""
                 SELECT m.*
                 FROM favorites f
                 JOIN media m ON m.id = f.media_id
                 WHERE f.user_id = ?
                 ORDER BY f.created_at DESC
             """)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Media m = new Media();
                m.id = rs.getInt("id");
                m.title = rs.getString("title");
                m.description = rs.getString("description");
                m.mediaType = rs.getString("media_type");
                m.releaseYear = rs.getInt("release_year");
                m.ageRestriction = rs.getInt("age_restriction");
                m.creatorId = rs.getInt("creator_id");
                list.add(m);
            }
        }

        return list;
    }
}
