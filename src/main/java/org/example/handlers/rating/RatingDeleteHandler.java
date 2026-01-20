package org.example.handlers.rating;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;import org.example.handlers.auth.AuthUtil;
import org.example.handlers.util.PathUtil;
import com.sun.net.httpserver.*;
import org.example.domain.User;
import org.example.persistence.RatingRepository;
import org.example.persistence.UserRepository;

import java.io.IOException;

public class RatingDeleteHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            handleInternal(exchange);
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);
        } finally {
            exchange.close();
        }
    }

    private void handleInternal(HttpExchange exchange) throws Exception {

        if (!exchange.getRequestMethod().equalsIgnoreCase("DELETE")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        // AUTH
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }
        String token = auth.substring("Bearer ".length()).trim();
        User user = UserRepository.getUserByToken(token);
        if (user == null) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        // query: id
        String query = exchange.getRequestURI().getQuery();
        Integer id = null;
        if (query != null) {
            for (String part : query.split("&")) {
                String[] kv = part.split("=", 2);
                if (kv.length == 2 && kv[0].equals("id")) {
                    try { id = Integer.parseInt(kv[1]); } catch (NumberFormatException ignored) {}
                }
            }
        }
        if (id == null || id <= 0) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        boolean ok = RatingRepository.delete(id, user.id);
        if (!ok) {
            exchange.sendResponseHeaders(403, -1);
            return;
        }

        String res = "{\"message\":\"rating deleted\"}";
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, res.getBytes().length);
        exchange.getResponseBody().write(res.getBytes());
    }
}
