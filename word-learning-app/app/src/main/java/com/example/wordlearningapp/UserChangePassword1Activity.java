package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonObject;

public class UserChangePassword1Activity extends AppCompatActivity {

    private EditText etContact, etVerifyCode;
    private Button btnVerify, btnSendCode;
    private TextView tvBackLogin, tvTitle;
    private String contact;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_change_password1);

        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "reset";

        etContact = findViewById(R.id.et_contact);
        etVerifyCode = findViewById(R.id.et_verify_code);
        btnVerify = findViewById(R.id.btn_verify);
        btnSendCode = findViewById(R.id.btn_send_code);
        tvBackLogin = findViewById(R.id.tv_back_login);
        tvTitle = findViewById(R.id.tv_title);

        if ("change".equals(mode)) {
            tvTitle.setText("修改密码 - 验证身份");
            tvBackLogin.setText("返回个人中心");
        } else {
            tvTitle.setText("找回密码 - 验证身份");
            tvBackLogin.setText("返回登录");
        }

        btnSendCode.setOnClickListener(v -> sendVerificationCode());
        btnVerify.setOnClickListener(v -> verifyIdentity());
        tvBackLogin.setOnClickListener(v -> {
            if ("change".equals(mode)) {
                finish();
            } else {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });
    }

    private void sendVerificationCode() {
        contact = etContact.getText().toString().trim();

        if (contact.isEmpty()) {
            Toast.makeText(this, "请输入手机号或邮箱", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSendCode.setEnabled(false);
        btnSendCode.setText("发送中...");

        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("contact", contact);

                JsonObject result = ApiClient.get().post("/api/user/send-verification-code", body);
                int code = result.has("code") ? result.get("code").getAsInt() : -1;
                String message = result.has("message") ? result.get("message").getAsString() : "发送失败";

                runOnUiThread(() -> {
                    btnSendCode.setEnabled(true);
                    btnSendCode.setText("发送验证码");

                    if (code == 200) {
                        Toast.makeText(this, "验证码已发送", Toast.LENGTH_SHORT).show();
                        startCountdown();
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSendCode.setEnabled(true);
                    btnSendCode.setText("发送验证码");
                    Toast.makeText(this, "网络连接失败", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void startCountdown() {
        btnSendCode.setEnabled(false);
        final int[] seconds = {60};

        runOnUiThread(() -> {
            btnSendCode.setText(seconds[0] + "s后重发");
        });

        new Thread(() -> {
            while (seconds[0] > 0) {
                try {
                    Thread.sleep(1000);
                    seconds[0]--;

                    runOnUiThread(() -> {
                        if (seconds[0] > 0) {
                            btnSendCode.setText(seconds[0] + "s后重发");
                        } else {
                            btnSendCode.setEnabled(true);
                            btnSendCode.setText("发送验证码");
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void verifyIdentity() {
        contact = etContact.getText().toString().trim();
        String verifyCode = etVerifyCode.getText().toString().trim();

        if (contact.isEmpty()) {
            Toast.makeText(this, "请输入手机号或邮箱", Toast.LENGTH_SHORT).show();
            return;
        }

        if (verifyCode.isEmpty()) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }

        btnVerify.setEnabled(false);
        btnVerify.setText("验证中...");

        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("contact", contact);
                body.addProperty("verifyCode", verifyCode);

                JsonObject result = ApiClient.get().post("/api/user/verify-code", body);
                int code = result.has("code") ? result.get("code").getAsInt() : -1;
                String message = result.has("message") ? result.get("message").getAsString() : "验证失败";

                runOnUiThread(() -> {
                    btnVerify.setEnabled(true);
                    btnVerify.setText("验证并下一步");

                    if (code == 200) {
                        Intent intent = new Intent(UserChangePassword1Activity.this, UserSetNewPasswordActivity.class);
                        intent.putExtra("contact", contact);
                        intent.putExtra("mode", mode);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnVerify.setEnabled(true);
                    btnVerify.setText("验证并下一步");
                    Toast.makeText(this, "网络连接失败", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}
