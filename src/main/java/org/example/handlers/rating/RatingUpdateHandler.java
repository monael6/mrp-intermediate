package org.example.handlers.rating;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;import org.example.handlers.auth.AuthUtil;
import org.example.handlers.util.PathUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import org.example.domain.User;
import org.example.persistence.RatingRepository;
import org.example.persistence.UserRepository;

import java.io.IOException;

public class RatingUpdateHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try { handleInternal(exchange); }
        catch (Exception e) { e.printStackTrace(); exchange.sendResponseHeaders(500, -1); }
        finally { exchange.close(); }
    }

    private void handleInternal(HttpExchange exchange) throws Exception {
        if (!exchange.getRequestMethod().equalsIgnoreCase("PUT")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        User user = AuthUtil.auth(exchange);
        if (user == null) return;

        Integer ratingId = PathUtil.getId(exchange, 3);
        if (ratingId == null) { exchange.sendResponseHeaders(400, -1); return; }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode body = mapper.readTree(exchange.getRequestBody());

        int stars = body.get("stars").asInt();
        String comment = body.has("comment") ? body.get("comment").asText() : null;

        if (stars < 1 || stars > 5) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        boolean ok = RatingRepository.updateById(ratingId, user.id, stars, comment);
        if (!ok) { exchange.sendResponseHeaders(403, -1); return; }

        exchange.sendResponseHeaders(200, -1);
    }
}
