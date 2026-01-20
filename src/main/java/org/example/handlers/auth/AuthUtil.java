package org.example.handlers.auth;

import com.sun.net.httpserver.HttpExchange;
import org.example.domain.User;
import org.example.persistence.UserRepository;

public class AuthUtil {

    public static User auth(HttpExchange exchange) throws Exception {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            exchange.sendResponseHeaders(401, -1);
            return null;
        }

        String token = auth.substring(7);
        User user = UserRepository.getUserByToken(token);
        if (user == null) {
            exchange.sendResponseHeaders(401, -1);
            return null;
        }
        return user;
    }
}
