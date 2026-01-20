package org.example.handlers.rating;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;import org.example.handlers.auth.AuthUtil;
import org.example.handlers.util.PathUtil;
import com.sun.net.httpserver.*;
import org.example.domain.User;
import org.example.persistence.LikeRepository;
import org.example.persistence.RatingRepository;
import org.example.persistence.UserRepository;

import java.io.IOException;

public class RatingUnlikeHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try { handleInternal(exchange); }
        catch (Exception e) { e.printStackTrace(); exchange.sendResponseHeaders(500, -1); }
        finally { exchange.close(); }
    }

    private void handleInternal(HttpExchange exchange) throws Exception {
        if (!exchange.getRequestMethod().equalsIgnoreCase("DELETE")) { exchange.sendResponseHeaders(405, -1); return; }

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) { exchange.sendResponseHeaders(401, -1); return; }
        String token = auth.substring("Bearer ".length()).trim();
        User user = UserRepository.getUserByToken(token);
        if (user == null) { exchange.sendResponseHeaders(401, -1); return; }

        Integer id = null;
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.startsWith("id=")) {
            try { id = Integer.parseInt(query.substring(3)); } catch (NumberFormatException ignored) {}
        }
        if (id == null || id <= 0) { exchange.sendResponseHeaders(400, -1); return; }

        if (RatingRepository.findById(id) == null) { exchange.sendResponseHeaders(404, -1); return; }

        LikeRepository.unlike(id, user.id);

        String res = "{\"message\":\"unliked\"}";
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, res.getBytes().length);
        exchange.getResponseBody().write(res.getBytes());
    }
}
