package org.example.tests;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiWalkthrough {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient client = HttpClient.newHttpClient();

    private static String token1;
    private static String token2;
    private static int mediaId;

    public static void main(String[] args) {
        try {
            System.out.println("=== STARTING API WALKTHROUGH (Robust) ===");

            // 1. Register Users
            String randomSuffix = "_" + System.currentTimeMillis();
            String user1Name = "Alice" + randomSuffix;
            String user2Name = "Bob" + randomSuffix;

            System.out.println("\n[1] Registering User 1 (" + user1Name + ")...");
            register(user1Name, "password");

            System.out.println("[2] Registering User 2 (" + user2Name + ")...");
            register(user2Name, "password");

            // 2. Login
            System.out.println("\n[3] Logging in User 1...");
            token1 = login(user1Name, "password");
            System.out.println("    Token 1 acquired.");

            System.out.println("[4] Logging in User 2...");
            token2 = login(user2Name, "password");
            System.out.println("    Token 2 acquired.");

            // 3. Create Media (User 1)
            String uniqueTitle = "Movie" + randomSuffix;
            System.out.println("\n[5] Creating Media '" + uniqueTitle + "' (User 1)...");
            mediaId = createMedia(token1, uniqueTitle, "movie");
            System.out.println("    Media created with ID: " + mediaId);

            // 4. Rate Media (User 2)
            System.out.println("\n[6] Rating Media " + mediaId + " (User 2)...");
            rateMedia(token2, mediaId, 5, "Amazing auto-test movie!");
            System.out.println("    Rating submitted.");

            // 5. Check Score (verify it's 5.0)
            System.out.println("\n[7] Verifying Media Score...");
            verifyScore(mediaId, 5.0);

            // 6. List Ratings to find ID
            System.out.println("\n[8] Listing Ratings...");
            int ratingId = findRatingId(token1, mediaId);
            System.out.println("     Found Rating ID: " + ratingId);

            // 7. Confirm Rating (User 1)
            System.out.println("\n[9] Confirming Rating " + ratingId + " (User 1)...");
            confirmRatingById(token1, ratingId);
            System.out.println("     Confirmed.");

            // 8. Verify Comment Visibility
            System.out.println("\n[10] Verifying Comment Visibility...");
            verifyCommentVisible(token1, mediaId, "Amazing auto-test movie!");

            System.out.println("\n=== SUCCESS! All steps passed. ===");

        } catch (Exception e) {
            System.err.println("\n!!! FAILURE !!!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void register(String username, String password) throws Exception {
        String json = "{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}";
        sendPost("/users/register", json, null, 201);
    }

    private static String login(String username, String password) throws Exception {
        String json = "{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}";
        String resp = sendPost("/users/login", json, null, 200);
        // Extract token
        Matcher m = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(resp);
        if (m.find())
            return m.group(1);
        throw new RuntimeException("Token not found in login response: " + resp);
    }

    private static int createMedia(String token, String title, String type) throws Exception {
        String json = "{" +
                "\"title\":\"" + title + "\"," +
                "\"description\":\"Desc\"," +
                "\"mediaType\":\"" + type + "\"," +
                "\"releaseYear\":2024," +
                "\"ageRestriction\":12," +
                "\"genres\":[\"Action\"]" +
                "}";
        String resp = sendPost("/media", json, token, 201);

        // Extract ID from JSON
        Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(resp);
        if (m.find())
            return Integer.parseInt(m.group(1));
        throw new RuntimeException("Media ID not found in create response: " + resp);
    }

    private static void rateMedia(String token, int mediaId, int stars, String comment) throws Exception {
        String json = "{\"stars\":" + stars + ", \"comment\":\"" + comment + "\"}";
        sendPost("/media/" + mediaId + "/rate", json, token, 201);
    }

    private static void verifyScore(int mediaId, double expected) throws Exception {
        // Must provide token for Media Get
        String resp = sendGet("/media/" + mediaId, token1, 200);

        // Extract score
        Matcher m = Pattern.compile("\"score\"\\s*:\\s*([\\d\\.]+)").matcher(resp);
        if (m.find()) {
            double score = Double.parseDouble(m.group(1));
            if (Math.abs(score - expected) > 0.1) {
                throw new RuntimeException("Score mismatch! Expected " + expected + " but got " + score);
            }
            System.out.println("    Score verified: " + score);
        } else {
            throw new RuntimeException("Score field not found in: " + resp);
        }
    }

    private static int findRatingId(String token, int mediaId) throws Exception {
        String resp = sendGet("/ratings?mediaId=" + mediaId, token, 200);
        Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(resp);
        if (m.find())
            return Integer.parseInt(m.group(1));
        throw new RuntimeException("No ratings found. Resp: " + resp);
    }

    private static void confirmRatingById(String token, int ratingId) throws Exception {
        sendPost("/ratings/" + ratingId + "/confirm", "", token, 200);
    }

    private static void verifyCommentVisible(String token, int mediaId, String expectedComment) throws Exception {
        String resp = sendGet("/ratings?mediaId=" + mediaId, token, 200);
        if (!resp.contains(expectedComment)) {
            throw new RuntimeException("Comment '" + expectedComment + "' not found in response: " + resp);
        }
        System.out.println("    Comment is visible.");
    }

    // HTTP Helpers

    private static String sendPost(String path, String json, String token, int expectedStatus) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
        if (token != null)
            builder.header("Authorization", "Bearer " + token);

        HttpResponse<String> resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != expectedStatus) {
            throw new RuntimeException(
                    "POST " + path + " failed. Status: " + resp.statusCode() + ", Body: " + resp.body());
        }
        return resp.body();
    }

    private static String sendGet(String path, String token, int expectedStatus) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET();
        if (token != null)
            builder.header("Authorization", "Bearer " + token);

        HttpResponse<String> resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != expectedStatus) {
            throw new RuntimeException(
                    "GET " + path + " failed. Status: " + resp.statusCode() + ", Body: " + resp.body());
        }
        return resp.body();
    }
}
