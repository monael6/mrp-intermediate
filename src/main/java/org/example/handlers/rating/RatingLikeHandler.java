package org.example.handlers.rating;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;import org.example.handlers.auth.AuthUtil;
import org.example.handlers.util.PathUtil;
import com.sun.net.httpserver.*;
import org.example.domain.User;
import org.example.persistence.LikeRepository;
import org.example.persistence.RatingRepository;

import java.io.IOException;

public class RatingLikeHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try { handleInternal(exchange); }
        catch (Exception e) { e.printStackTrace(); exchange.sendResponseHeaders(500, -1); }
        finally { exchange.close(); }
    }

    private void handleInternal(HttpExchange exchange) throws Exception {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        User user = AuthUtil.auth(exchange);
        if (user == null) return;

        // Spec: /api/ratings/{ratingId}/like  -> ratingId ist index 3
        Integer ratingId = PathUtil.getId(exchange, 3);
        if (ratingId == null || ratingId <= 0) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        if (RatingRepository.findById(ratingId) == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        LikeRepository.like(ratingId, user.id);

        String res = "{\"message\":\"liked\"}";
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, res.getBytes().length);
        exchange.getResponseBody().write(res.getBytes());
    }
}
