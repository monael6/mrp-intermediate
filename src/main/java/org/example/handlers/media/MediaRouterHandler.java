package org.example.handlers.media;
import com.fasterxml.jackson.databind.ObjectMapper;import org.example.handlers.auth.AuthUtil;
import org.example.handlers.util.PathUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class MediaRouterHandler implements HttpHandler {

    private final MediaCreateHandler create = new MediaCreateHandler();
    private final MediaListHandler list = new MediaListHandler();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if ("POST".equalsIgnoreCase(method)) {
            create.handle(exchange);
            return;
        }

        if ("GET".equalsIgnoreCase(method)) {
            list.handle(exchange);
            return;
        }

        exchange.sendResponseHeaders(405, -1);
        exchange.close();
    }
}
