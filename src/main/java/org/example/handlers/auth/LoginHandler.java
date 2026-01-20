package org.example.handlers.auth;

import org.example.domain.User;
import org.example.domain.UserCredentials;
import org.example.persistence.UserRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;

import java.io.IOException;

public class LoginHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            handleInternal(exchange);
        } catch (Exception e) {
            e.printStackTrace();
            String res = "{\"error\":\"server error\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(500, res.getBytes().length);
            exchange.getResponseBody().write(res.getBytes());
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

        // basic validation
        if (creds.username == null || creds.password == null ||
                creds.username.isBlank() || creds.password.isBlank()) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        // user aus db holen
        User dbUser = UserRepository.findByUsername(creds.username);

        // username prüfen
        if (dbUser == null) {
            String res = "{\"error\":\"invalid username\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(401, res.getBytes().length);
            exchange.getResponseBody().write(res.getBytes());
            return;
        }

        // pwd prüfen
        if (!dbUser.password.equals(creds.password)) {
            String res = "{\"error\":\"wrong password\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(401, res.getBytes().length);
            exchange.getResponseBody().write(res.getBytes());
            return;
        }

        // Token erzeugen + speichern (DB persistent)
        String token = dbUser.username + "-mrpToken";
        UserRepository.saveToken(token, dbUser);

        String res = "{\"token\":\"" + token + "\"}";
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, res.getBytes().length);
        exchange.getResponseBody().write(res.getBytes());
    }
}
