package org.example.handlers.auth;

import org.example.domain.User;
import org.example.domain.UserCredentials;
import org.example.persistence.UserRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;

import java.io.IOException;

public class RegisterHandler implements HttpHandler {

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

        // nur POST erlaubt
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        UserCredentials creds;

        // BODY PARSEN
        try {
            String body = new String(exchange.getRequestBody().readAllBytes());
            creds = mapper.readValue(body, UserCredentials.class);
        } catch (Exception e) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        // einfache Validierung
        if (creds.username == null || creds.password == null ||
                creds.username.isBlank() || creds.password.isBlank()) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        // User bauen
        User u = new User();
        u.username = creds.username;
        u.password = creds.password;

        try {
            UserRepository.save(u);

            String res = "{\"message\":\"user registered\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(201, res.getBytes().length);
            exchange.getResponseBody().write(res.getBytes());

        } catch (Exception e) {
            // username schon vorhanden
            String res = "{\"error\":\"username exists\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(400, res.getBytes().length);
            exchange.getResponseBody().write(res.getBytes());
        }
    }
}
