package org.example.handlers;

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

        User user = UserRepository.getUserByToken(auth.substring(7));
        if (user == null) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

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

        try {
            MediaRepository.save(m);
            String res = "{\"message\":\"media created\"}";
            exchange.sendResponseHeaders(201, res.length());
            exchange.getResponseBody().write(res.getBytes());
        } catch (Exception e) {
            exchange.sendResponseHeaders(500, -1);
        }

        exchange.close();
    }
}
