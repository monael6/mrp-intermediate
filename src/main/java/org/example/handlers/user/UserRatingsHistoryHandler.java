package org.example.handlers.user;
import com.fasterxml.jackson.databind.ObjectMapper;import org.example.handlers.auth.AuthUtil;
import org.example.handlers.util.PathUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import org.example.domain.User;
import org.example.persistence.UserRatingsRepository;
import org.example.persistence.UserRepository;

import java.io.IOException;

public class UserRatingsHistoryHandler implements HttpHandler {

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

        if (userId != user.id) { exchange.sendResponseHeaders(403, -1); return; }

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(UserRatingsRepository.listUserRatings(userId));

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, json.getBytes().length);
        exchange.getResponseBody().write(json.getBytes());
    }

    private Integer parseUserId(HttpExchange exchange) {
        // /api/users/{id}/ratings
        String[] parts = exchange.getRequestURI().getPath().split("/");
        if (parts.length >= 5 && "users".equalsIgnoreCase(parts[2]) && "ratings".equalsIgnoreCase(parts[4])) {
            try { return Integer.parseInt(parts[3]); } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
