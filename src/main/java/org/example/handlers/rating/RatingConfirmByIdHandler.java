package org.example.handlers.rating;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;import org.example.handlers.auth.AuthUtil;
import org.example.handlers.util.PathUtil;
import com.sun.net.httpserver.*;
import org.example.domain.User;
import org.example.persistence.RatingRepository;

import java.io.IOException;

public class RatingConfirmByIdHandler implements HttpHandler {

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

        Integer ratingId = PathUtil.getId(exchange, 3);
        if (ratingId == null) { exchange.sendResponseHeaders(400, -1); return; }

        boolean ok = RatingRepository.confirmComment(ratingId, user.id);
        if (!ok) { exchange.sendResponseHeaders(403, -1); return; }

        exchange.sendResponseHeaders(200, -1);
    }
}
