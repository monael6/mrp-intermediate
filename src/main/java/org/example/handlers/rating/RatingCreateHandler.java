package org.example.handlers.rating;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.example.handlers.auth.AuthUtil;
import org.example.handlers.util.PathUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import org.example.domain.User;
import org.example.persistence.MediaRepository;
import org.example.persistence.RatingRepository;
import org.example.persistence.UserRepository;

import java.io.IOException;

public class RatingCreateHandler implements HttpHandler {

    static class CreateRatingRequest {
        public int mediaId;
        public int stars;
        public String comment;
    }

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

        // METHOD CHECK
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

        // BODY
        ObjectMapper mapper = new ObjectMapper();
        CreateRatingRequest req;
        try {
            String body = new String(exchange.getRequestBody().readAllBytes());
            req = mapper.readValue(body, CreateRatingRequest.class);
        } catch (Exception e) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        // OPTIONAL: ID from Path /api/media/{id}/rate
        if (req.mediaId <= 0) {
            Integer pathId = PathUtil.getId(exchange, 3); // /api/media/{id}/rate -> pos 3
            if (pathId != null) {
                req.mediaId = pathId;
            }
        }
        // unlogische werte abgelehnt
        if (req.mediaId <= 0 || req.stars < 1 || req.stars > 5) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }
        // existiert media?
        if (MediaRepository.findById(req.mediaId) == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        // upsert (1 rating per user+media), comment_confirmed resets to false
        RatingRepository.upsert(req.mediaId, user.id, req.stars, req.comment);

        String res = "{\"message\":\"rating saved\"}";
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(201, res.getBytes().length);
        exchange.getResponseBody().write(res.getBytes());
    }
}
