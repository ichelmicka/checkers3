package com.example.integration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GamePersistenceClient {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String baseUrl; // e.g. "http://localhost:8080"

    public GamePersistenceClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Long createGame(String blackPlayer, String whitePlayer) throws Exception {
        String json = String.format("{\"black\":\"%s\",\"white\":\"%s\"}", escape(blackPlayer), escape(whitePlayer));
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/api/games"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> r = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() / 100 != 2) throw new RuntimeException("createGame failed: " + r.body());

        String loc = r.headers().firstValue("Location").orElse(r.headers().firstValue("location").orElse(null));
        if (loc != null && !loc.isBlank()) {
            try {
                String idStr = loc.substring(loc.lastIndexOf('/') + 1);
                return Long.parseLong(idStr);
            } catch (Exception ignored) {}
        }

        // fallback: spróbuj znaleźć "id":<number> w body (niebezpieczne, ale lepsze niż null)
        String body = r.body();
        if (body != null) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(body);
            if (m.find()) {
                return Long.parseLong(m.group(1));
            }
        }

        return null;
    }


    public void persistMove(Long gameId, int fromRow, int fromCol, int toRow, int toCol, boolean capture, String extra) throws Exception {
        String json = String.format("{\"fromRow\":%d,\"fromCol\":%d,\"toRow\":%d,\"toCol\":%d,\"capture\":%b,\"extra\":\"%s\"}",
                fromRow, fromCol, toRow, toCol, capture, escape(extra == null ? "" : extra));
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/api/games/" + gameId + "/moves"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> r = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() / 100 != 2) throw new RuntimeException("persistMove failed: " + r.body());
    }

    public void finishGame(Long gameId, String result) throws Exception {
        String json = String.format("{\"result\":\"%s\"}", escape(result));
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/api/games/" + gameId + "/finish"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> r = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() / 100 != 2) throw new RuntimeException("finishGame failed: " + r.body());
    }

    private String escape(String s) {
        return s.replace("\"", "\\\"");
    }
}