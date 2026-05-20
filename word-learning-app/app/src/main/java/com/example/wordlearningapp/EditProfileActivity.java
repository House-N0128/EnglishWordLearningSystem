package com.example.wordlearningapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonObject;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etNickname, etPhone, etEmail, etUserId;
    private Button btnSave, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etNickname = findViewById(R.id.et_nickname);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        etUserId = findViewById(R.id.et_user_id);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveProfile());
        btnCancel.setOnClickListener(v -> finish());

        loadCurrentProfile();
    }

    private void loadCurrentProfile() {
        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/user/profile");
                if (res.get("code").getAsInt() == 200) {
                    JsonObject u = res.getAsJsonObject("data");
                    runOnUiThread(() -> {
                        if (u.has("userName") && !u.get("userName").isJsonNull()) {
                            etNickname.setText(u.get("userName").getAsString());
                        }
                        if (u.has("phoneNumber") && !u.get("phoneNumber").isJsonNull()) {
                            etPhone.setText(u.get("phoneNumber").getAsString());
                        }
                        if (u.has("email") && !u.get("email").isJsonNull()) {
                            etEmail.setText(u.get("email").getAsString());
                        }
                        if (u.has("userId") && !u.get("userId").isJsonNull()) {
                            etUserId.setText(u.get("userId").getAsString());
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "加载信息失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void saveProfile() {
        String nickname = etNickname.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (nickname.isEmpty()) {
            Toast.makeText(this, "请输入昵称", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!phone.matches("\\d{11}")) {
            Toast.makeText(this, "请输入正确的11位手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!email.contains("@")) {
            Toast.makeText(this, "请输入正确的邮箱地址", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("保存中...");

        JsonObject body = new JsonObject();
        body.addProperty("userName", nickname);
        body.addProperty("phoneNumber", phone);
        body.addProperty("email", email);

        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().put("/api/user/profile", body);
                int code = res.has("code") ? res.get("code").getAsInt() : -1;
                String message = res.has("message") ? res.get("message").getAsString() : "保存失败";

                runOnUiThread(() -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("保存修改");
                    if (code == 200) {
                        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("保存修改");
                    Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}
