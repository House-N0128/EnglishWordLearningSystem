package com.example.wordlearningapp.api;

import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String BASE_URL = "http://10.0.2.2:8080";
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

    public JsonObject uploadFile(String path, String fileFieldName, String fileName,
                                  InputStream fileStream, String mediaType,
                                  java.util.Map<String, String> formFields) throws IOException {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        byte[] fileBytes = readBytes(fileStream);
        builder.addFormDataPart(fileFieldName, fileName,
                RequestBody.create(fileBytes, MediaType.parse(mediaType)));

        if (formFields != null) {
            for (java.util.Map.Entry<String, String> entry : formFields.entrySet()) {
                builder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }

        RequestBody requestBody = builder.build();
        Request req = addHeaders().url(BASE_URL + path).post(requestBody).build();

        try (Response res = client.newCall(req).execute()) {
            String body = res.body() != null ? res.body().string() : "{}";
            return gson.fromJson(body, JsonObject.class);
        }
    }

    private byte[] readBytes(InputStream is) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int n;
        while ((n = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, n);
        }
        return buffer.toByteArray();
    }
}
