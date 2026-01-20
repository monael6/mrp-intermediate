package org.example.handlers.favorite;
import org.example.handlers.auth.AuthUtil;
import org.example.handlers.util.PathUtil;
import com.sun.net.httpserver.*;
import org.example.domain.User;
import org.example.persistence.FavoriteRepository;
import org.example.persistence.MediaRepository;
import org.example.persistence.UserRepository;

import java.io.IOException;

public class FavoriteRemoveHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try { handleInternal(exchange); }
        catch (Exception e) { e.printStackTrace(); exchange.sendResponseHeaders(500, -1); }
        finally { exchange.close(); }
    }

    private void handleInternal(HttpExchange exchange) throws Exception {
        if (!exchange.getRequestMethod().equalsIgnoreCase("DELETE")) { exchange.sendResponseHeaders(405, -1); return; }

        // AUTH
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) { exchange.sendResponseHeaders(401, -1); return; }
        String token = auth.substring("Bearer ".length()).trim();
        User user = UserRepository.getUserByToken(token);
        if (user == null) { exchange.sendResponseHeaders(401, -1); return; }

        // mediaId aus PATH oder fallback aus QUERY
        Integer id = parseMediaId(exchange);
        if (id == null || id <= 0) { exchange.sendResponseHeaders(400, -1); return; }

        if (MediaRepository.findById(id) == null) { exchange.sendResponseHeaders(404, -1); return; }

        FavoriteRepository.remove(id, user.id);

        // Spec: 204 No Content
        exchange.sendResponseHeaders(204, -1);
    }

    private Integer parseMediaId(HttpExchange exchange) {
        // Spec: /api/media/{id}/favorite
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        // ["", "api", "media", "{id}", "favorite"]
        if (parts.length >= 5 && "favorite".equalsIgnoreCase(parts[4])) {
            try { return Integer.parseInt(parts[3]); } catch (NumberFormatException ignored) {}
        }

        // Fallback alt: ?id=123 oder id=123&...
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            for (String part : query.split("&")) {
                String[] kv = part.split("=", 2);
                if (kv.length == 2 && kv[0].equals("id")) {
                    try { return Integer.parseInt(kv[1]); } catch (NumberFormatException ignored) {}
                }
            }
        }

        return null;
    }
}
