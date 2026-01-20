package org.example.handlers.rating;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;import org.example.handlers.auth.AuthUtil;
import org.example.handlers.util.PathUtil;
import com.sun.net.httpserver.*;

import java.io.IOException;

public class RatingsIdRouterHandler implements HttpHandler {

    private final RatingUpdateHandler update = new RatingUpdateHandler();
    private final RatingDeleteByIdHandler delete = new RatingDeleteByIdHandler();
    private final RatingConfirmByIdHandler confirm = new RatingConfirmByIdHandler();
    private final RatingLikeHandler like = new RatingLikeHandler();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String[] parts = exchange.getRequestURI().getPath().split("/");

        if (parts.length < 4) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }

        if (parts.length >= 5 && "confirm".equals(parts[4])) {
            confirm.handle(exchange);
            return;
        }

        if (parts.length >= 5 && "like".equals(parts[4])) {
            like.handle(exchange);
            return;
        }

        if (exchange.getRequestMethod().equalsIgnoreCase("PUT")) {
            update.handle(exchange);
            return;
        }

        if (exchange.getRequestMethod().equalsIgnoreCase("DELETE")) {
            delete.handle(exchange);
            return;
        }

        exchange.sendResponseHeaders(405, -1);
        exchange.close();
    }
}
