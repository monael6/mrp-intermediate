package org.example.handlers.media;
import com.fasterxml.jackson.databind.ObjectMapper;import org.example.handlers.auth.AuthUtil;
import org.example.handlers.util.PathUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import org.example.domain.Media;
import org.example.domain.User;
import org.example.persistence.MediaRepository;
import org.example.persistence.UserRepository;

import java.io.IOException;

public class MediaUpdateHandler implements HttpHandler {

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

        if (!exchange.getRequestMethod().equalsIgnoreCase("PUT")) {
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

        // BODY PARSEN
        ObjectMapper mapper = new ObjectMapper();
        Media m;
        try {
            String body = new String(exchange.getRequestBody().readAllBytes());
            m = mapper.readValue(body, Media.class);
        } catch (Exception e) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        // DB MEDIA HOLEN
        Media dbMedia = MediaRepository.findById(id);
        if (dbMedia == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        // CREATOR CHECK
        if (dbMedia.creatorId != user.id) {
            exchange.sendResponseHeaders(403, -1);
            return;
        }

        // ID + creatorId fix setzen
        m.id = id;
        m.creatorId = dbMedia.creatorId;

        MediaRepository.update(m);

        String res = "{\"message\":\"media updated\"}";
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, res.getBytes().length);
        exchange.getResponseBody().write(res.getBytes());
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
