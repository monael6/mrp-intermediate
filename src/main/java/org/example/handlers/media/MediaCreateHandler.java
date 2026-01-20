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

public class MediaCreateHandler implements HttpHandler {

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

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
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

        // CREATOR SETZEN
        m.creatorId = user.id;

        // OPTIONAL: genres darf null sein, repo handled das
        int mediaId = MediaRepository.save(m);

        String res = "{\"message\":\"media created\",\"id\":" + mediaId + "}";
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(201, res.getBytes().length);
        exchange.getResponseBody().write(res.getBytes());
    }
}
