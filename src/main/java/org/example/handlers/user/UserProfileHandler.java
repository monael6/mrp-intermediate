package org.example.handlers.user;
import com.fasterxml.jackson.databind.ObjectMapper;import org.example.handlers.auth.AuthUtil;
import org.example.handlers.util.PathUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import org.example.domain.User;
import org.example.persistence.UserProfileRepository;
import org.example.persistence.UserRepository;

import java.io.IOException;
import java.util.HashMap;

public class UserProfileHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try { handleInternal(exchange); }
        catch (Exception e) { e.printStackTrace(); exchange.sendResponseHeaders(500, -1); }
        finally { exchange.close(); }
    }

    private void handleInternal(HttpExchange exchange) throws Exception {

        String method = exchange.getRequestMethod();
        if (!method.equalsIgnoreCase("GET") && !method.equalsIgnoreCase("PUT")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        // AUTH
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) { exchange.sendResponseHeaders(401, -1); return; }
        String token = auth.substring("Bearer ".length()).trim();
        User user = UserRepository.getUserByToken(token);
        if (user == null) { exchange.sendResponseHeaders(401, -1); return; }

        Integer userId = parseUserId(exchange);
        if (userId == null || userId <= 0) { exchange.sendResponseHeaders(400, -1); return; }

        // nur eigenes Profil
        if (userId != user.id) { exchange.sendResponseHeaders(403, -1); return; }

        ObjectMapper mapper = new ObjectMapper();

        if (method.equalsIgnoreCase("GET")) {
            HashMap<String, Object> profile = UserProfileRepository.getProfile(userId);
            if (profile == null) { exchange.sendResponseHeaders(404, -1); return; }

            String json = mapper.writeValueAsString(profile);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.getBytes().length);
            exchange.getResponseBody().write(json.getBytes());
            return;
        }

        // PUT
        String body = new String(exchange.getRequestBody().readAllBytes());
        JsonNode node;
        try { node = mapper.readTree(body); }
        catch (Exception e) { exchange.sendResponseHeaders(400, -1); return; }

        String email = node.has("email") && !node.get("email").isNull() ? node.get("email").asText() : null;
        String favoriteGenre = node.has("favoriteGenre") && !node.get("favoriteGenre").isNull() ? node.get("favoriteGenre").asText() : null;

        // minimal validation: allow nulls (setzt dann null)
        UserProfileRepository.updateProfile(userId, email, favoriteGenre);

        String res = "{\"message\":\"profile updated\"}";
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, res.getBytes().length);
        exchange.getResponseBody().write(res.getBytes());
    }

    private Integer parseUserId(HttpExchange exchange) {
        // /api/users/{id}/profile
        String[] parts = exchange.getRequestURI().getPath().split("/");
        if (parts.length >= 5 && "users".equalsIgnoreCase(parts[2]) && "profile".equalsIgnoreCase(parts[4])) {
            try { return Integer.parseInt(parts[3]); } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
