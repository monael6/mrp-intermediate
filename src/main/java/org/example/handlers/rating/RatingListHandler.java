package org.example.handlers.rating;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;import org.example.handlers.auth.AuthUtil;
import org.example.handlers.util.PathUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import org.example.domain.Rating;
import org.example.domain.User;
import org.example.persistence.RatingRepository;
import org.example.persistence.UserRepository;

import java.io.IOException;
import java.util.ArrayList;

public class RatingListHandler implements HttpHandler {

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

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
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

        // query: mediaId
        String query = exchange.getRequestURI().getQuery();
        Integer mediaId = null;
        if (query != null) {
            for (String part : query.split("&")) {
                String[] kv = part.split("=", 2);
                if (kv.length == 2 && kv[0].equals("mediaId")) {
                    try { mediaId = Integer.parseInt(kv[1]); } catch (NumberFormatException ignored) {}
                }
            }
        }
        if (mediaId == null || mediaId <= 0) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        ArrayList<Rating> list = RatingRepository.findByMediaIdPublic(mediaId);

        //owner sieht seinen kommentar immer, die anderen sehen nur confirmed
        for (Rating r : list) {
            if (r.userId != user.id && !r.commentConfirmed) {
                r.comment = null;
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(list);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, json.getBytes().length);
        exchange.getResponseBody().write(json.getBytes());
    }
}
