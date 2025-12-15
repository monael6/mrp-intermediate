package org.example.handlers;
import org.example.domain.User;
import org.example.domain.UserCredentials;
import org.example.persistence.UserRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;

import java.io.IOException;

public class LoginHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        //check if POST
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        //body lesen + json-> object
        String body = new String(exchange.getRequestBody().readAllBytes());
        UserCredentials creds = mapper.readValue(body, UserCredentials.class);

        try {
            //user aus db holen
            User dbUser = UserRepository.findByUsername(creds.username);

            //user/pwd prüfen
            if (dbUser == null) {
                String res = "{\"error\":\"invalid username\"}";
                exchange.sendResponseHeaders(401, res.length());
                exchange.getResponseBody().write(res.getBytes());
                exchange.close();
                return;
            }

            if (!dbUser.password.equals(creds.password)) {
                String res = "{\"error\":\"wrong password\"}";
                exchange.sendResponseHeaders(401, res.length());
                exchange.getResponseBody().write(res.getBytes());
                exchange.close();
                return;
            }

            // Token erzeugen + speichern
            String token = dbUser.username + "-mrpToken";
            UserRepository.saveToken(token, dbUser);

            String res = "{\"token\":\"" + token + "\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, res.length());
            exchange.getResponseBody().write(res.getBytes());

        } catch (Exception e) {
            String res = "{\"error\":\"server error\"}";
            exchange.sendResponseHeaders(500, res.length());
            exchange.getResponseBody().write(res.getBytes());
        }

        exchange.close();
    }
}
