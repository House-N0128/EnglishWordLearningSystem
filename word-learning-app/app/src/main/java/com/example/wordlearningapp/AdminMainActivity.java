package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonObject;

public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (!AuthManager.get().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ((TextView) findViewById(R.id.toolbar_title)).setText("管理员首页");
        LinearLayout content = findViewById(R.id.content_area);

        TextView welcome = new TextView(this);
        welcome.setText("欢迎，管理员 " + AuthManager.get().getUserId());
        welcome.setTextSize(18);
        welcome.setTextColor(0xFF318af8);
        welcome.setGravity(Gravity.CENTER);
        welcome.setPadding(0, 20, 0, 20);
        content.addView(welcome);

        TextView desc = new TextView(this);
        desc.setText("平台管理功能开发中...");
        desc.setTextSize(15);
        desc.setTextColor(0xFF8899aa);
        desc.setGravity(Gravity.CENTER);
        content.addView(desc);

        findViewById(R.id.toolbar_back).setOnClickListener(v -> {
            AuthManager.get().clearAuth();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
