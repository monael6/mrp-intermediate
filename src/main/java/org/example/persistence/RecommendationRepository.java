package org.example.persistence;

import org.example.db.Database;
import org.example.domain.Media;

import java.sql.*;
import java.util.ArrayList;

public class RecommendationRepository {

    public static ArrayList<Media> byGenre(int userId, int limit) throws Exception {
        ArrayList<Media> list = new ArrayList<>();

        try (Connection conn = Database.connect()) {

            // favorite_genre holen
            String fav = null;
            try (PreparedStatement u = conn.prepareStatement("SELECT favorite_genre FROM users WHERE id = ?")) {
                u.setInt(1, userId);
                ResultSet urs = u.executeQuery();
                if (urs.next())
                    fav = urs.getString("favorite_genre");
            }

            if (fav == null || fav.isBlank())
                return list;

            PreparedStatement stmt = conn.prepareStatement(
                    """
                            SELECT
                                m.id, m.title, m.description, m.media_type, m.release_year,
                                m.age_restriction, m.creator_id,
                                COALESCE(AVG(r.stars),0) AS score
                            FROM media m
                            JOIN media_genres g ON g.media_id = m.id
                            LEFT JOIN ratings r ON r.media_id = m.id
                            WHERE g.genre ILIKE ?
                            GROUP BY m.id
                            ORDER BY score DESC, m.title ASC
                            LIMIT ?
                            """);

            stmt.setString(1, fav.trim());
            stmt.setInt(2, limit);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Media m = mapMedia(rs);
                m.genres = getGenres(conn, m.id);
                list.add(m);
            }
        }

        return list;
    }

    public static ArrayList<Media> byContent(int userId, int limit) throws Exception {
        ArrayList<Media> list = new ArrayList<>();

        try (Connection conn = Database.connect()) {

            // wir nehmen als "User Taste" die Top-1 Kategorie aus seinen Ratings (höchste
            // Sterne) als Seed
            // (minimal & funktioniert ohne extra Profil-Logik)
            Seed seed = getSeedFromRatings(conn, userId);
            if (seed == null)
                return list;

            PreparedStatement stmt = conn.prepareStatement(
                    """
                            SELECT
                                m.id, m.title, m.description, m.media_type, m.release_year,
                                m.age_restriction, m.creator_id,
                                COALESCE(AVG(r.stars),0) AS score
                            FROM media m
                            JOIN media_genres g ON g.media_id = m.id
                            LEFT JOIN ratings r ON r.media_id = m.id
                            WHERE m.media_type = ?
                              AND m.age_restriction <= ?
                              AND g.genre = ?
                              AND m.id <> ?
                            GROUP BY m.id
                            ORDER BY score DESC, m.title ASC
                            LIMIT ?
                            """);

            stmt.setString(1, seed.mediaType);
            stmt.setInt(2, seed.ageRestriction);
            stmt.setString(3, seed.genre);
            stmt.setInt(4, seed.mediaId);
            stmt.setInt(5, limit);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Media m = mapMedia(rs);
                m.genres = getGenres(conn, m.id);
                list.add(m);
            }
        }

        return list;
    }

    // -------- helpers --------

    private static Media mapMedia(ResultSet rs) throws SQLException {
        Media m = new Media();
        m.id = rs.getInt("id");
        m.title = rs.getString("title");
        m.description = rs.getString("description");
        m.mediaType = rs.getString("media_type");
        m.releaseYear = rs.getInt("release_year");
        m.ageRestriction = rs.getInt("age_restriction");
        m.creatorId = rs.getInt("creator_id");
        m.score = rs.getDouble("score");
        return m;
    }

    private static ArrayList<String> getGenres(Connection conn, int mediaId) throws SQLException {
        ArrayList<String> genres = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT genre FROM media_genres WHERE media_id = ?");
        stmt.setInt(1, mediaId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
            genres.add(rs.getString("genre"));
        return genres;
    }

    private static Seed getSeedFromRatings(Connection conn, int userId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                """
                        SELECT m.id AS media_id, m.media_type, m.age_restriction, g.genre
                        FROM ratings r
                        JOIN media m ON m.id = r.media_id
                        JOIN media_genres g ON g.media_id = m.id
                        WHERE r.user_id = ?
                        ORDER BY r.stars DESC, r.created_at DESC
                        LIMIT 1
                        """);
        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next())
            return null;

        Seed s = new Seed();
        s.mediaId = rs.getInt("media_id");
        s.mediaType = rs.getString("media_type");
        s.ageRestriction = rs.getInt("age_restriction");
        s.genre = rs.getString("genre");
        return s;
    }

    private static class Seed {
        int mediaId;
        String mediaType;
        int ageRestriction;
        String genre;
    }
}
