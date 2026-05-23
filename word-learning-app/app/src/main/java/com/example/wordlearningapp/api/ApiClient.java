package com.example.wordlearningapp.api;

import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String BASE_URL = "http://10.115.171.176:8080";
    private static ApiClient instance;

    private final OkHttpClient client;
    private final Gson gson = new Gson();

    public static synchronized ApiClient get() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    private ApiClient() {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    private Request.Builder addHeaders() {
        Request.Builder builder = new Request.Builder();
        AuthManager am = AuthManager.get();
        if (am != null && am.isLoggedIn()) {
            builder.addHeader("X-User-Id", am.getUserId());
            builder.addHeader("X-User-Role", am.getRole());
        }
        return builder;
    }

    public JsonObject get(String path) throws IOException {
        Request req = addHeaders().url(BASE_URL + path).get().build();
        try (Response res = client.newCall(req).execute()) {
            String body = res.body() != null ? res.body().string() : "{}";
            return gson.fromJson(body, JsonObject.class);
        }
    }

    public JsonObject post(String path, JsonObject json) throws IOException {
        String jsonStr = json.toString();
        RequestBody body = RequestBody.create(jsonStr, JSON);
        Request req = addHeaders().url(BASE_URL + path).post(body).build();
        try (Response res = client.newCall(req).execute()) {
            String respBody = res.body() != null ? res.body().string() : "{}";
            return gson.fromJson(respBody, JsonObject.class);
        }
    }

    public JsonObject put(String path, JsonObject json) throws IOException {
        String jsonStr = json.toString();
        RequestBody body = RequestBody.create(jsonStr, JSON);
        Request req = addHeaders().url(BASE_URL + path).put(body).build();
        try (Response res = client.newCall(req).execute()) {
            String respBody = res.body() != null ? res.body().string() : "{}";
            return gson.fromJson(respBody, JsonObject.class);
        }
    }

    public JsonObject delete(String path, JsonObject json) throws IOException {
        String jsonStr = json != null ? json.toString() : "{}";
        RequestBody body = RequestBody.create(jsonStr, JSON);
        Request req = addHeaders().url(BASE_URL + path).delete(body).build();
        try (Response res = client.newCall(req).execute()) {
            String respBody = res.body() != null ? res.body().string() : "{}";
            return gson.fromJson(respBody, JsonObject.class);
        }
    }

    public String getBaseUrl() {
        return BASE_URL;
    }
}
