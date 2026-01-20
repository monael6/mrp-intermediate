package org.example.handlers.media;
import com.fasterxml.jackson.databind.ObjectMapper;import org.example.handlers.auth.AuthUtil;
import org.example.handlers.util.PathUtil;
import org.example.persistence.MediaRepository;
import org.example.persistence.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import org.example.domain.Media;
import org.example.domain.User;

import java.io.IOException;

public class MediaGetHandler implements HttpHandler {

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

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
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

        Integer id = parseIdFromPath(exchange);
        if (id == null || id <= 0) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        Media m = MediaRepository.findById(id);
        if (m == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(m);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, json.getBytes().length);
        exchange.getResponseBody().write(json.getBytes());
    }

    private Integer parseIdFromPath(HttpExchange exchange) {
        String[] parts = exchange.getRequestURI().getPath().split("/");
        if (parts.length == 0) return null;
        String last = parts[parts.length - 1];
        try {
            return Integer.parseInt(last);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
