package org.example.handlers.user;
import com.fasterxml.jackson.databind.ObjectMapper;import org.example.handlers.favorite.FavoriteListHandler;
import org.example.handlers.reco.RecommendationHandler;import org.example.handlers.auth.AuthUtil;
import org.example.handlers.util.PathUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class UserIdRouterHandler implements HttpHandler {

    private final FavoriteListHandler favorites = new FavoriteListHandler();
    private final RecommendationHandler recommendations = new RecommendationHandler();
    private final UserProfileHandler profile = new UserProfileHandler();
    private final UserRatingsHistoryHandler ratings = new UserRatingsHistoryHandler();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        String[] parts = path.split("/");
        if (parts.length < 4) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }

        try {
            Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            exchange.sendResponseHeaders(400, -1);
            exchange.close();
            return;
        }

        if (parts.length >= 5 && "favorites".equalsIgnoreCase(parts[4])) {
            if ("GET".equalsIgnoreCase(method)) { favorites.handle(exchange); return; }
            exchange.sendResponseHeaders(405, -1); exchange.close(); return;
        }

        if (parts.length >= 5 && "recommendations".equalsIgnoreCase(parts[4])) {
            if ("GET".equalsIgnoreCase(method)) { recommendations.handle(exchange); return; }
            exchange.sendResponseHeaders(405, -1); exchange.close(); return;
        }

        if (parts.length >= 5 && "profile".equalsIgnoreCase(parts[4])) {
            if ("GET".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) { profile.handle(exchange); return; }
            exchange.sendResponseHeaders(405, -1); exchange.close(); return;
        }

        if (parts.length >= 5 && "ratings".equalsIgnoreCase(parts[4])) {
            if ("GET".equalsIgnoreCase(method)) { ratings.handle(exchange); return; }
            exchange.sendResponseHeaders(405, -1); exchange.close(); return;
        }

        exchange.sendResponseHeaders(404, -1);
        exchange.close();
    }
}
