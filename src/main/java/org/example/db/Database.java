package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String URL = "jdbc:postgresql://127.0.0.1:5332/mrp";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";

    public static Connection connect() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("Connected to DB: " + conn.getCatalog());
        return conn;
    }

    public static void init() {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {

                // USERS
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id SERIAL PRIMARY KEY,
                        username TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL
                    );
                """);

                // MEDIA
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS media (
                        id SERIAL PRIMARY KEY,
                        title TEXT NOT NULL,
                        description TEXT,
                        media_type TEXT NOT NULL,
                        release_year INTEGER,
                        age_restriction INTEGER,
                        creator_id INTEGER NOT NULL,
                        FOREIGN KEY (creator_id) REFERENCES users(id)
                    );
                """);

                // TOKENS
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS tokens (
                        token TEXT PRIMARY KEY,
                        user_id INTEGER NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    );
                """);

                // RATINGS
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS ratings (
                        id SERIAL PRIMARY KEY,
                        media_id INTEGER NOT NULL,
                        user_id INTEGER NOT NULL,
                        stars INTEGER NOT NULL CHECK (stars >= 1 AND stars <= 5),
                        comment TEXT,
                        comment_confirmed BOOLEAN DEFAULT FALSE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE (media_id, user_id),
                        FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE CASCADE,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    );
                """);

                // RATING LIKES
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS rating_likes (
                        rating_id INTEGER NOT NULL,
                        user_id INTEGER NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (rating_id, user_id),
                        FOREIGN KEY (rating_id) REFERENCES ratings(id) ON DELETE CASCADE,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    );
                """);

                // FAVORITES
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS favorites (
                        media_id INTEGER NOT NULL,
                        user_id INTEGER NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (media_id, user_id),
                        FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE CASCADE,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    );
                """);

                // ADD COLUMNS (separate statements)
                stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS email TEXT;");
                stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS favorite_genre TEXT;");

                // MEDIA_GENRES
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS media_genres (
                        media_id INTEGER NOT NULL,
                        genre TEXT NOT NULL,
                        PRIMARY KEY (media_id, genre),
                        FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE CASCADE
                    );
                """);

                conn.commit();
                System.out.println("Tables ready.");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
