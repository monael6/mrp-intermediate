package org.example.handlers;

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

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        //AUTH
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        String token = auth.substring("Bearer ".length());
        User user = UserRepository.getUserByToken(token);
        if (user == null) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        // Query Parameter holen
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.contains("id=")) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        int id = Integer.parseInt(query.substring(3));

        try {
            Media m = MediaRepository.findById(id);

            if (m == null) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(m);

            exchange.sendResponseHeaders(200, json.length());
            exchange.getResponseBody().write(json.getBytes());

        } catch (Exception e) {
            exchange.sendResponseHeaders(500, -1);
        }

        exchange.close();
    }
}
