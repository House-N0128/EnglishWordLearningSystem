package com.example.wordlearningapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonObject;

public class RegisterActivity extends AppCompatActivity {

    private EditText etAccount, etPassword, etConfirmPw, etNickname, etPhone, etEmail;
    private TextView tvError, tvSuccess;
    private Button btnRegister, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etAccount = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);
        etConfirmPw = findViewById(R.id.et_confirm_pw);
        etNickname = findViewById(R.id.et_nickname);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        tvError = findViewById(R.id.tv_error);
        tvSuccess = findViewById(R.id.tv_success);
        btnRegister = findViewById(R.id.btn_register);
        btnBack = findViewById(R.id.btn_back);

        btnRegister.setOnClickListener(v -> doRegister());
        btnBack.setOnClickListener(v -> finish());
    }

    private void doRegister() {
        String account = etAccount.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPw = etConfirmPw.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        tvError.setVisibility(View.GONE);
        tvSuccess.setVisibility(View.GONE);

        if (account.isEmpty() || password.isEmpty() || confirmPw.isEmpty()
                || nickname.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            showError("请填写所有必填字段");
            return;
        }
        if (!password.equals(confirmPw)) {
            showError("两次输入的密码不一致");
            return;
        }
        if (password.length() < 6) {
            showError("密码长度至少6位");
            return;
        }
        if (!phone.matches("\\d{11}")) {
            showError("请输入正确的11位手机号");
            return;
        }
        if (!email.contains("@")) {
            showError("请输入正确的邮箱地址");
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("注册中...");

        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("userId", account);
                body.addProperty("loginPassword", password);
                body.addProperty("userName", nickname);
                body.addProperty("phoneNumber", phone);
                body.addProperty("email", email);

                JsonObject result = ApiClient.get().post("/api/user/register", body);
                int code = result.has("code") ? result.get("code").getAsInt() : -1;
                String message = result.has("message") ? result.get("message").getAsString() : "注册失败";

                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("注册");
                    if (code == 200) {
                        tvSuccess.setText("注册成功！即将返回登录...");
                        tvSuccess.setVisibility(View.VISIBLE);
                        btnRegister.postDelayed(this::finish, 1500);
                    } else {
                        showError(message);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("注册");
                    showError("网络连接失败");
                });
            }
        }).start();
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}
