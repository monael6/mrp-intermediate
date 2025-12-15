package org.example;

import org.example.db.Database;
import org.example.handlers.*;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {

        //erstellt die tabellen
        Database.init();

        //server starten auf port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // USERS ( router dupe um handler zu regestrieren für api request)
        server.createContext("/api/users/register", new RegisterHandler());
        server.createContext("/api/users/login", new LoginHandler());

        // MEDIA
        server.createContext("/api/media/create", new MediaCreateHandler());
        server.createContext("/api/media/update", new MediaUpdateHandler());
        server.createContext("/api/media/delete", new MediaDeleteHandler());
        server.createContext("/api/media/get", new MediaGetHandler());
        server.createContext("/api/media/list", new MediaListHandler());


        server.start();
        System.out.println("Server running...");
    }
}
