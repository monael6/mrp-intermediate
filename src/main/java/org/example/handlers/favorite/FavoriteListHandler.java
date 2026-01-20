package org.example.handlers.favorite;
import org.example.handlers.auth.AuthUtil;
import org.example.handlers.util.PathUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import org.example.domain.Media;
import org.example.domain.User;
import org.example.persistence.FavoriteRepository;
import org.example.persistence.UserRepository;

import java.io.IOException;
import java.util.ArrayList;

public class FavoriteListHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try { handleInternal(exchange); }
        catch (Exception e) { e.printStackTrace(); exchange.sendResponseHeaders(500, -1); }
        finally { exchange.close(); }
    }

    private void handleInternal(HttpExchange exchange) throws Exception {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) { exchange.sendResponseHeaders(405, -1); return; }

        // AUTH
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) { exchange.sendResponseHeaders(401, -1); return; }
        String token = auth.substring("Bearer ".length()).trim();
        User user = UserRepository.getUserByToken(token);
        if (user == null) { exchange.sendResponseHeaders(401, -1); return; }

        // Spec: /api/users/{userId}/favorites
        Integer requestedUserId = parseUserId(exchange);

        // Wenn URL keinen userId hat (alte Route), dann default = token user
        if (requestedUserId == null) requestedUserId = user.id;

        if (requestedUserId <= 0) { exchange.sendResponseHeaders(400, -1); return; }

        // nur eigene Favorites erlauben
        if (requestedUserId != user.id) { exchange.sendResponseHeaders(403, -1); return; }

        ArrayList<Media> list = FavoriteRepository.listFavorites(requestedUserId);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(list);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, json.getBytes().length);
        exchange.getResponseBody().write(json.getBytes());
    }

    private Integer parseUserId(HttpExchange exchange) {
        // erwartet: /api/users/{id}/favorites
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        // ["", "api", "users", "{id}", "favorites"]
        if (parts.length >= 5 && "users".equalsIgnoreCase(parts[2]) && "favorites".equalsIgnoreCase(parts[4])) {
            try { return Integer.parseInt(parts[3]); } catch (NumberFormatException ignored) {}
        }

        return null;
    }
}
