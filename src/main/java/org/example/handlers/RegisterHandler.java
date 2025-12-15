package org.example.handlers;
import org.example.domain.User;
import org.example.domain.UserCredentials;
import org.example.persistence.UserRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;

import java.io.IOException;

public class RegisterHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        //check ob POST (GET, PUT, etc. nicht erlaubt -> sonst 405)
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        //body lesen  und json-> java objekt
        String body = new String(exchange.getRequestBody().readAllBytes());
        UserCredentials creds = mapper.readValue(body, UserCredentials.class);

        //user bauen
        User u = new User();
        u.username = creds.username;
        u.password = creds.password;

        try {
            //user speichern
            UserRepository.save(u);

            //erfolgsantwort
            String res = "{\"message\":\"user registered\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(201, res.length());
            exchange.getResponseBody().write(res.getBytes());

            //fehlerbehandlung
        } catch (Exception e) {
            String res = "{\"error\":\"username exists\"}";
            exchange.sendResponseHeaders(400, res.length());
            exchange.getResponseBody().write(res.getBytes());
        }

        exchange.close();
    }
}

