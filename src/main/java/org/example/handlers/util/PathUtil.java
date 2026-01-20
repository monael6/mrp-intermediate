package org.example.handlers.util;

import com.sun.net.httpserver.HttpExchange;

public class PathUtil {

    public static Integer getId(HttpExchange exchange, int index) {
        try {
            return Integer.parseInt(exchange.getRequestURI().getPath().split("/")[index]);
        } catch (Exception e) {
            return null;
        }
    }
}
