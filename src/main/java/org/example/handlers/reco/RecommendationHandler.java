package org.example.handlers.reco;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import org.example.domain.Media;
import org.example.domain.User;
import org.example.persistence.RecommendationRepository;
import org.example.persistence.UserRepository;

import java.io.IOException;
import java.util.ArrayList;

public class RecommendationHandler implements HttpHandler {

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

        Integer userId = parseUserId(exchange);
        if (userId == null || userId <= 0) { exchange.sendResponseHeaders(400, -1); return; }

        // nur eigene recommendations
        if (userId != user.id) { exchange.sendResponseHeaders(403, -1); return; }

        String type = getQueryParam(exchange.getRequestURI().getQuery(), "type");
        if (type == null) type = "genre";
        type = type.trim().toLowerCase();

        ArrayList<Media> result;
        if (type.equals("content")) {
            result = RecommendationRepository.byContent(userId, 10);
        } else if (type.equals("genre")) {
            result = RecommendationRepository.byGenre(userId, 10);
        } else {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(result);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, json.getBytes().length);
        exchange.getResponseBody().write(json.getBytes());
    }

    private Integer parseUserId(HttpExchange exchange) {
        // /api/users/{id}/recommendations
        String[] parts = exchange.getRequestURI().getPath().split("/");
        if (parts.length >= 5 && "users".equalsIgnoreCase(parts[2]) && "recommendations".equalsIgnoreCase(parts[4])) {
            try { return Integer.parseInt(parts[3]); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private String getQueryParam(String query, String key) {
        if (query == null || query.isBlank()) return null;
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length >= 1 && kv[0].equals(key)) {
                return kv.length == 2 ? kv[1] : "";
            }
        }
        return null;
    }
}
