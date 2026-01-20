package org.example.handlers.media;
import com.fasterxml.jackson.databind.ObjectMapper;import org.example.handlers.auth.AuthUtil;
import org.example.handlers.util.PathUtil;
import org.example.persistence.MediaRepository;
import org.example.persistence.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import org.example.domain.Media;
import org.example.domain.User;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class MediaListHandler implements HttpHandler {

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

        // Query Params
        HashMap<String, String> qp = parseQuery(exchange.getRequestURI().getQuery());

        String title = qp.get("title");
        String genre = qp.get("genre");
        String mediaType = qp.get("mediaType");

        Integer releaseYear = parseInt(qp.get("releaseYear"));
        Integer ageRestriction = parseInt(qp.get("ageRestriction"));
        Double rating = parseDouble(qp.get("rating"));

        String sortBy = qp.get("sortBy"); // title | year | score

        ArrayList<Media> result = MediaRepository.search(
                title,
                genre,
                mediaType,
                releaseYear,
                ageRestriction,
                rating,
                sortBy
        );

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(result);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, json.getBytes().length);
        exchange.getResponseBody().write(json.getBytes());
    }

    private HashMap<String, String> parseQuery(String query) {
        HashMap<String, String> map = new HashMap<>();
        if (query == null || query.isBlank()) return map;

        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String val = kv.length == 2 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(key, val);
        }
        return map;
    }

    private Integer parseInt(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }

    private Double parseDouble(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return null; }
    }
}
