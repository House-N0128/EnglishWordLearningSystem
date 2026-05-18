package com.example.wordlearningapp.util;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {
    private static final String PREFS_NAME = "word_auth";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences prefs;

    private static AuthManager instance;

    public static synchronized void init(Context ctx) {
        if (instance == null) instance = new AuthManager(ctx.getApplicationContext());
    }

    public static AuthManager get() {
        return instance;
    }

    private AuthManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setAuth(String userId, String role) {
        prefs.edit().putString(KEY_USER_ID, userId).putString(KEY_ROLE, role).apply();
    }

    public String getUserId() { return prefs.getString(KEY_USER_ID, null); }
    public String getRole() { return prefs.getString(KEY_ROLE, null); }

    public boolean isLoggedIn() {
        return getUserId() != null && !getUserId().isEmpty();
    }

    public void clearAuth() {
        prefs.edit().remove(KEY_USER_ID).remove(KEY_ROLE).apply();
    }
}
