package org.example.handlers;

import com.sun.net.httpserver.*;
import org.example.domain.Media;
import org.example.domain.User;
import org.example.persistence.MediaRepository;
import org.example.persistence.UserRepository;

import java.io.IOException;

public class MediaDeleteHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("DELETE")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

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

        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("id=")) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        int id = Integer.parseInt(query.substring(3));

        try {
            Media m = MediaRepository.findById(id);
            if (m == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            if (m.creatorId != user.id) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }

            MediaRepository.delete(id);

            String res = "{\"message\":\"media deleted\"}";
            exchange.sendResponseHeaders(200, res.length());
            exchange.getResponseBody().write(res.getBytes());

        } catch (Exception e) {
            exchange.sendResponseHeaders(500, -1);
        }

        exchange.close();
    }
}
