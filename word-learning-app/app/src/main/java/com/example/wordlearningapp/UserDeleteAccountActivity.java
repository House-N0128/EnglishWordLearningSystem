package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonObject;

public class UserDeleteAccountActivity extends AppCompatActivity {

    private EditText etPassword;
    private CheckBox cbConfirm;
    private Button btnConfirmDelete;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_delete_account);

        initViews();
        initListeners();
    }

    private void initViews() {
        etPassword = findViewById(R.id.et_password);
        cbConfirm = findViewById(R.id.cb_confirm);
        btnConfirmDelete = findViewById(R.id.btn_confirm_delete);
        btnCancel = findViewById(R.id.btn_cancel);

        btnConfirmDelete.setEnabled(false);
    }

    private void initListeners() {
        cbConfirm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnConfirmDelete.setEnabled(isChecked);
        });

        btnCancel.setOnClickListener(v -> finish());

        btnConfirmDelete.setOnClickListener(v -> {
            String password = etPassword.getText().toString().trim();
            if (password.isEmpty()) {
                Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                return;
            }
            deleteAccount(password);
        });
    }

    private void deleteAccount(String password) {
        new Thread(() -> {
            try {
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("password", password);

                JsonObject res = ApiClient.get().delete("/api/user/account", requestBody);
                int code = res.has("code") ? res.get("code").getAsInt() : -1;
                String message = res.has("message") ? res.get("message").getAsString() : "操作失败";

                runOnUiThread(() -> {
                    if (code == 200) {
                        Toast.makeText(this, "账号已注销", Toast.LENGTH_SHORT).show();
                        AuthManager.get().clearAuth();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
