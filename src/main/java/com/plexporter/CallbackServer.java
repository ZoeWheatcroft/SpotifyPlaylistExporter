package com.plexporter;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class CallbackServer {

    public static volatile String authorizationCode;
    public static HttpServer server;
    public void begin() throws Exception {
        server = HttpServer.create(new InetSocketAddress(8888), 0);

        server.createContext("/callback", CallbackServer::handleCallback);

        server.start();

        System.out.println("Waiting for Spotify login...");
        
    }

    public void end() {
        server.stop(20);
    }

    private static void handleCallback(HttpExchange exchange) {

        try {

            System.out.println("Processing callback");

            Map<String, String> params =
                    parseQuery(exchange.getRequestURI().getQuery());
            
            authorizationCode = params.get("code");

            System.out.println("Authorization Code:");
            System.out.println(authorizationCode);

            String response =
                    "Login successful. You can close this tab.";

            exchange.sendResponseHeaders(200, response.length());

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> parseQuery(String query) {

        Map<String, String> map = new HashMap<>();

        if (query == null)
            return map;

        for (String pair : query.split("&")) {

            String[] parts = pair.split("=", 2);

            if (parts.length == 2)
                map.put(parts[0], parts[1]);
        }

        return map;
    }
}