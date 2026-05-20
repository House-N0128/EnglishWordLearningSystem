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

public class UserSetNewPasswordActivity extends AppCompatActivity {

    private EditText etNewPassword, etConfirmPassword;
    private Button btnReset;
    private TextView tvBackLogin, tvContact, tvTitle;
    private String contact;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_set_new_password);

        contact = getIntent().getStringExtra("contact");
        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "reset";

        if (contact == null || contact.isEmpty()) {
            Toast.makeText(this, "验证信息丢失，请重新验证", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnReset = findViewById(R.id.btn_reset);
        tvBackLogin = findViewById(R.id.tv_back_login);
        tvContact = findViewById(R.id.tv_contact);
        tvTitle = findViewById(R.id.tv_title);

        if ("change".equals(mode)) {
            tvTitle.setText("设置新密码");
            tvContact.setText("为账号 " + contact + " 设置新密码");
            tvBackLogin.setText("返回个人中心");
        } else {
            tvTitle.setText("设置新密码");
            tvContact.setText("为账号 " + contact + " 设置新密码");
            tvBackLogin.setText("返回登录");
        }

        btnReset.setOnClickListener(v -> resetPassword());
        tvBackLogin.setOnClickListener(v -> {
            if ("change".equals(mode)) {
                finish();
            } else {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void resetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (newPassword.isEmpty()) {
            Toast.makeText(this, "请输入新密码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "请确认新密码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "密码长度至少6位", Toast.LENGTH_SHORT).show();
            return;
        }

        btnReset.setEnabled(false);
        btnReset.setText("重置中...");

        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("contact", contact);
                body.addProperty("newPassword", newPassword);

                JsonObject result = ApiClient.get().post("/api/user/reset-password", body);
                int code = result.has("code") ? result.get("code").getAsInt() : -1;
                String message = result.has("message") ? result.get("message").getAsString() : "重置失败";

                runOnUiThread(() -> {
                    btnReset.setEnabled(true);
                    btnReset.setText("重置密码");

                    if (code == 200) {
                        Toast.makeText(this, "密码重置成功", Toast.LENGTH_SHORT).show();

                        if ("change".equals(mode)) {
                            finish();
                        } else {
                            Intent intent = new Intent(UserSetNewPasswordActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnReset.setEnabled(true);
                    btnReset.setText("重置密码");
                    Toast.makeText(this, "网络连接失败", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}
