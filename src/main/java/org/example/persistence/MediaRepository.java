package org.example.persistence;

import org.example.db.Database;
import org.example.domain.Media;

import java.sql.*;
import java.util.ArrayList;

public class MediaRepository {

    public static void save(Media m) throws Exception {
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     """
                     INSERT INTO media
                     (title, description, media_type, release_year, age_restriction, creator_id)
                     VALUES (?, ?, ?, ?, ?, ?)
                     """
             )) {

            stmt.setString(1, m.title);
            stmt.setString(2, m.description);
            stmt.setString(3, m.mediaType);
            stmt.setInt(4, m.releaseYear);
            stmt.setInt(5, m.ageRestriction);
            stmt.setInt(6, m.creatorId); // 🔥 GANZ WICHTIG

            stmt.executeUpdate();
        }
    }

    public static Media findById(int id) throws Exception {
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM media WHERE id = ?"
             )) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) return null;

            Media m = new Media();
            m.id = rs.getInt("id");
            m.title = rs.getString("title");
            m.description = rs.getString("description");
            m.mediaType = rs.getString("media_type");
            m.releaseYear = rs.getInt("release_year");
            m.ageRestriction = rs.getInt("age_restriction");
            m.creatorId = rs.getInt("creator_id"); // 🔥 MUSS DA SEIN

            return m;
        }
    }

    public static ArrayList<Media> findAll() throws Exception {
        ArrayList<Media> list = new ArrayList<>();

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM media"
             );
             ResultSet rs = stmt.executeQuery()) {

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

    public static void update(Media m) throws Exception {
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     """
                     UPDATE media SET
                     title = ?, description = ?, media_type = ?, release_year = ?, age_restriction = ?
                     WHERE id = ?
                     """
             )) {

            stmt.setString(1, m.title);
            stmt.setString(2, m.description);
            stmt.setString(3, m.mediaType);
            stmt.setInt(4, m.releaseYear);
            stmt.setInt(5, m.ageRestriction);
            stmt.setInt(6, m.id);

            stmt.executeUpdate();
        }
    }

    public static void delete(int id) throws Exception {
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM media WHERE id = ?"
             )) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
