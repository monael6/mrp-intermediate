package org.example.handlers;

import org.example.persistence.MediaRepository;
import org.example.persistence.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import org.example.domain.Media;
import org.example.domain.User;

import java.io.IOException;
import java.util.ArrayList;

public class MediaListHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        //check if GET
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

        String token = auth.substring("Bearer ".length());
        User user = UserRepository.getUserByToken(token);
        if (user == null) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        try {
            //db zugriff
            ArrayList<Media> all = MediaRepository.findAll();

            ObjectMapper mapper = new ObjectMapper();
            //json response
            String json = mapper.writeValueAsString(all);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.length());
            exchange.getResponseBody().write(json.getBytes());

        } catch (Exception e) {
            exchange.sendResponseHeaders(500, -1);
        }

        exchange.close();
    }
}
