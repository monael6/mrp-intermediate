package org.example.db;

import java.sql.*;

public class Database {

    private static final String URL = "jdbc:postgresql://localhost:5332/mrp";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";

    public static Connection connect() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("Connected to DB: " + conn.getCatalog());
        return conn;
    }

    public static void init() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

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

            System.out.println("Tables ready.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
