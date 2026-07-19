package com.noprobit.analyzers.analyzers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.noprobit.analyzers.analyzers.model.AnalysisResult;
import java.net.HttpURLConnection;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class AnalysisController {
    private final PurposeAnalyser analyser;
    private final Gson gson = new Gson();

    public AnalysisController(PurposeAnalyser analyser) {
        this.analyser = analyser;
    }

    public HttpHandler analyzeHandler() {
        return exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                handleAnalyzeRequest(exchange);
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        };
    }

    public HttpHandler healthHandler() {
        return exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                JsonObject response = new JsonObject();
                response.addProperty("status", "UP");
                response.addProperty("service", "AnalysisService");
                response.addProperty("version", "1.0");

                sendResponse(exchange, 200, response.toString());
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        };
    }

    private void handleAnalyzeRequest(HttpExchange exchange) throws IOException {
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonObject request = JsonParser.parseString(body).getAsJsonObject();

            String className = request.has("className") ? request.get("className").getAsString() : "";
            String extendsClass = request.has("extendsClass") ? request.get("extendsClass").getAsString() : null;

            if (className.isEmpty()) {
                sendError(exchange, 400, "className is required");
                return;
            }

            AnalysisResult result = analyser.analyzeAndLog(className, extendsClass);

            JsonObject response = new JsonObject();
            response.addProperty("actualName", result.actualName);
            response.addProperty("suggestedName", result.suggestedName);
            response.addProperty("purpose", result.purpose);
            response.addProperty("extendsClass", result.extendsClass != null ? result.extendsClass : "");

            sendResponse(exchange, 200, response.toString());

        } catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            sendResponse(exchange, 400, error.toString());
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, body.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        String body = error.toString();

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, body.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
    }
}
