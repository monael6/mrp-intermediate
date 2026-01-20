package org.example.persistence;

import org.example.db.Database;
import org.example.domain.Media;

import java.sql.*;
import java.util.ArrayList;

public class MediaRepository {

    // ========================
    // CREATE
    // ========================
    public static int save(Media m) throws Exception {
        try (Connection conn = Database.connect()) {

            PreparedStatement stmt = conn.prepareStatement(
                    """
                            INSERT INTO media (title, description, media_type, release_year, age_restriction, creator_id)
                            VALUES (?, ?, ?, ?, ?, ?)
                            RETURNING id;
                            """);

            stmt.setString(1, m.title);
            stmt.setString(2, m.description);
            stmt.setString(3, m.mediaType);
            stmt.setInt(4, m.releaseYear);
            stmt.setInt(5, m.ageRestriction);
            stmt.setInt(6, m.creatorId);

            ResultSet rs = stmt.executeQuery();
            rs.next();
            int mediaId = rs.getInt("id");

            if (m.genres != null) {
                PreparedStatement gStmt = conn.prepareStatement(
                        "INSERT INTO media_genres (media_id, genre) VALUES (?, ?)");

                for (String genre : m.genres) {
                    gStmt.setInt(1, mediaId);
                    gStmt.setString(2, genre);
                    gStmt.executeUpdate();
                }
            }

            return mediaId;
        }
    }

    // ========================
    // READ BY ID
    // ========================
    public static Media findById(int id) throws Exception {
        try (Connection conn = Database.connect()) {

            PreparedStatement stmt = conn.prepareStatement(
                    """
                            SELECT m.*, COALESCE(AVG(r.stars), 0) as score
                            FROM media m
                            LEFT JOIN ratings r ON r.media_id = m.id
                            WHERE m.id = ?
                            GROUP BY m.id
                            """);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next())
                return null;

            Media m = mapMedia(rs);
            m.genres = getGenres(conn, id);

            return m;
        }
    }

    // ========================
    // READ ALL (basic)
    // ========================
    public static ArrayList<Media> findAll() throws Exception {
        ArrayList<Media> list = new ArrayList<>();

        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(
                        """
                                SELECT m.*, COALESCE(AVG(r.stars), 0) as score
                                FROM media m
                                LEFT JOIN ratings r ON r.media_id = m.id
                                GROUP BY m.id
                                """);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Media m = mapMedia(rs);
                m.genres = getGenres(conn, m.id);
                list.add(m);
            }
        }

        return list;
    }

    // ========================
    // SEARCH / FILTER / SORT
    // ========================
    public static ArrayList<Media> search(
            String title,
            String genre,
            String mediaType,
            Integer releaseYear,
            Integer ageRestriction,
            Double minRating,
            String sortBy) throws Exception {

        ArrayList<Media> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("""
                    SELECT
                        m.id, m.title, m.description, m.media_type, m.release_year,
                        m.age_restriction, m.creator_id,
                        COALESCE(AVG(r.stars), 0) AS score
                    FROM media m
                    LEFT JOIN ratings r ON r.media_id = m.id
                    LEFT JOIN media_genres g ON g.media_id = m.id
                    WHERE 1=1
                """);

        ArrayList<Object> params = new ArrayList<>();

        if (title != null && !title.isBlank()) {
            sql.append(" AND m.title ILIKE ? ");
            params.add("%" + title.trim() + "%");
        }

        if (genre != null && !genre.isBlank()) {
            sql.append(" AND g.genre ILIKE ? ");
            params.add("%" + genre.trim() + "%");
        }

        if (mediaType != null && !mediaType.isBlank()) {
            sql.append(" AND m.media_type = ? ");
            params.add(mediaType.trim());
        }

        if (releaseYear != null) {
            sql.append(" AND m.release_year = ? ");
            params.add(releaseYear);
        }

        if (ageRestriction != null) {
            sql.append(" AND m.age_restriction <= ? ");
            params.add(ageRestriction);
        }

        sql.append(" GROUP BY m.id ");

        if (minRating != null) {
            sql.append(" HAVING COALESCE(AVG(r.stars),0) >= ? ");
            params.add(minRating);
        }

        if (sortBy == null)
            sortBy = "";
        sortBy = sortBy.toLowerCase();

        if (sortBy.equals("title")) {
            sql.append(" ORDER BY m.title ASC ");
        } else if (sortBy.equals("year")) {
            sql.append(" ORDER BY m.release_year ASC ");
        } else if (sortBy.equals("score")) {
            sql.append(" ORDER BY score DESC ");
        } else {
            sql.append(" ORDER BY m.id ASC ");
        }

        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String)
                    stmt.setString(i + 1, (String) p);
                else if (p instanceof Integer)
                    stmt.setInt(i + 1, (Integer) p);
                else if (p instanceof Double)
                    stmt.setDouble(i + 1, (Double) p);
                else
                    stmt.setObject(i + 1, p);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Media m = mapMedia(rs);
                m.genres = getGenres(conn, m.id);
                list.add(m);
            }
        }

        return list;
    }

    // ========================
    // UPDATE
    // ========================
    public static void update(Media m) throws Exception {
        try (Connection conn = Database.connect()) {

            PreparedStatement stmt = conn.prepareStatement(
                    """
                            UPDATE media SET
                                title = ?, description = ?, media_type = ?, release_year = ?, age_restriction = ?
                            WHERE id = ?
                            """);

            stmt.setString(1, m.title);
            stmt.setString(2, m.description);
            stmt.setString(3, m.mediaType);
            stmt.setInt(4, m.releaseYear);
            stmt.setInt(5, m.ageRestriction);
            stmt.setInt(6, m.id);
            stmt.executeUpdate();

            PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM media_genres WHERE media_id = ?");
            del.setInt(1, m.id);
            del.executeUpdate();

            if (m.genres != null) {
                PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO media_genres (media_id, genre) VALUES (?, ?)");

                for (String genre : m.genres) {
                    ins.setInt(1, m.id);
                    ins.setString(2, genre);
                    ins.executeUpdate();
                }
            }
        }
    }

    // ========================
    // DELETE
    // ========================
    public static void delete(int id) throws Exception {
        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM media WHERE id = ?")) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // ========================
    // HELPERS
    // ========================
    private static Media mapMedia(ResultSet rs) throws SQLException {
        Media m = new Media();
        m.id = rs.getInt("id");
        m.title = rs.getString("title");
        m.description = rs.getString("description");
        m.mediaType = rs.getString("media_type");
        m.releaseYear = rs.getInt("release_year");
        m.ageRestriction = rs.getInt("age_restriction");
        m.creatorId = rs.getInt("creator_id");
        try {
            m.score = rs.getDouble("score");
        } catch (SQLException e) {
            // Fallback if column not present (e.g. if partial object loaded, though we
            // fixed queries)
            m.score = 0.0;
        }
        return m;
    }

    private static ArrayList<String> getGenres(Connection conn, int mediaId) throws SQLException {
        ArrayList<String> genres = new ArrayList<>();

        PreparedStatement stmt = conn.prepareStatement(
                "SELECT genre FROM media_genres WHERE media_id = ?");
        stmt.setInt(1, mediaId);

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            genres.add(rs.getString("genre"));
        }

        return genres;
    }
}
