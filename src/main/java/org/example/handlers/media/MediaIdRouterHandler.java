package org.example.handlers.media;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.handlers.favorite.FavoriteAddHandler;
import org.example.handlers.favorite.FavoriteRemoveHandler;
import org.example.handlers.auth.AuthUtil;
import org.example.handlers.rating.RatingCreateHandler;
import org.example.handlers.util.PathUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class MediaIdRouterHandler implements HttpHandler {

    private final MediaGetHandler get = new MediaGetHandler();
    private final MediaUpdateHandler update = new MediaUpdateHandler();
    private final MediaDeleteHandler delete = new MediaDeleteHandler();

    private final FavoriteAddHandler favAdd = new FavoriteAddHandler();
    private final FavoriteRemoveHandler favRemove = new FavoriteRemoveHandler();
    private final RatingCreateHandler rate = new RatingCreateHandler();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath(); // z.B. /api/media/1 oder /api/media/1/favorite
        String method = exchange.getRequestMethod();

        // erwartete Teile: ["", "api", "media", "{id}", ...]
        String[] parts = path.split("/");
        if (parts.length < 4) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }

        // id check
        try {
            Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            exchange.sendResponseHeaders(400, -1);
            exchange.close();
            return;
        }

        // /api/media/{id}/rate
        if (parts.length >= 5 && "rate".equalsIgnoreCase(parts[4])) {
            if ("POST".equalsIgnoreCase(method)) {
                rate.handle(exchange);
                return;
            }
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        // /api/media/{id}/favorite
        if (parts.length >= 5 && "favorite".equalsIgnoreCase(parts[4])) {
            if ("POST".equalsIgnoreCase(method)) {
                favAdd.handle(exchange);
                return;
            }
            if ("DELETE".equalsIgnoreCase(method)) {
                favRemove.handle(exchange);
                return;
            }
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        // /api/media/{id}
        if ("GET".equalsIgnoreCase(method)) {
            get.handle(exchange);
            return;
        }
        if ("PUT".equalsIgnoreCase(method)) {
            update.handle(exchange);
            return;
        }
        if ("DELETE".equalsIgnoreCase(method)) {
            delete.handle(exchange);
            return;
        }

        exchange.sendResponseHeaders(405, -1);
        exchange.close();
    }
}
