package org.example;

import org.example.db.Database;
import org.example.handlers.auth.*;
import org.example.handlers.user.*;
import org.example.handlers.media.*;
import org.example.handlers.rating.*;
import org.example.handlers.leaderboard.*;
import org.example.handlers.reco.*;
import org.example.handlers.favorite.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {

        Database.init();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // AUTH
        server.createContext("/api/users/register", new RegisterHandler());
        server.createContext("/api/users/login", new LoginHandler());

        // USERS (/api/users/{id}/...)
        server.createContext("/api/users/", new UserIdRouterHandler());

        // MEDIA (/api/media + /api/media/{id} + /api/media/{id}/favorite)
        server.createContext("/api/media", new MediaRouterHandler());
        server.createContext("/api/media/", new MediaIdRouterHandler());

        // RATINGS (/api/ratings/{id} + /api/ratings/{id}/confirm +
        // /api/ratings/{id}/like)
        // RATINGS (/api/ratings?mediaId=...)
        server.createContext("/api/ratings", new RatingListHandler());
        // /api/ratings/{id}
        server.createContext("/api/ratings/", new RatingsIdRouterHandler());

        // LEADERBOARD
        server.createContext("/api/leaderboard", new LeaderboardHandler());

        server.start();
        System.out.println("Server running...");
    }
}
