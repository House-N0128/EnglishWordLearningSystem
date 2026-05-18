package com.example.wordlearningapp;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonObject;

public class WordDetailActivity extends AppCompatActivity {

    private LinearLayout contentArea;
    private String wordId;
    private JsonObject currentWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        wordId = getIntent().getStringExtra("wordId");
        ((TextView) findViewById(R.id.toolbar_title)).setText("单词详情");
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());
        contentArea = findViewById(R.id.content_area);

        if (wordId == null) {
            showText("缺少单词参数");
            return;
        }
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/words/" + wordId);
                if (res.get("code").getAsInt() == 200) {
                    currentWord = res.getAsJsonObject("data");
                    runOnUiThread(() -> buildUI(currentWord));
                } else {
                    runOnUiThread(() -> showText("单词不存在"));
                }
            } catch (Exception e) {
                runOnUiThread(() -> showText("加载失败"));
            }
        }).start();
    }

    private void buildUI(JsonObject w) {
        contentArea.removeAllViews();

        // Title
        TextView title = new TextView(this);
        title.setText(w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "");
        title.setTextSize(22);
        title.setTextColor(0xFF318af8);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 16, 0, 24);
        contentArea.addView(title);

        addField("英文拼写", w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "");
        addField("中文释义", w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "");
        addField("音标", w.has("phoneticSymbol") ? w.get("phoneticSymbol").getAsString() : "");
        addField("例句", w.has("exampleSentence") && !w.get("exampleSentence").isJsonNull()
                ? w.get("exampleSentence").getAsString() : "");

        // Audio button
        String audioUrl = w.has("wordPronunciation") && !w.get("wordPronunciation").isJsonNull()
                ? w.get("wordPronunciation").getAsString() : null;
        if (audioUrl != null && !audioUrl.isEmpty()) {
            Button btnAudio = new Button(this);
            btnAudio.setText("▶ 播放音频");
            btnAudio.setTextColor(0xFFFFFFFF);
            btnAudio.setBackgroundColor(0xFF318af8);
            btnAudio.setOnClickListener(v -> playAudio(audioUrl));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            p.setMargins(0, 12, 0, 12);
            btnAudio.setLayoutParams(p);
            contentArea.addView(btnAudio);
        }

        // Collect button
        Button btnCollect = new Button(this);
        btnCollect.setText("⭐ 收藏单词");
        btnCollect.setTextColor(0xFFFFFFFF);
        btnCollect.setBackgroundColor(0xFF039c83);
        btnCollect.setTextSize(17);
        btnCollect.setPadding(13, 13, 13, 13);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cp.setMargins(0, 24, 0, 0);
        btnCollect.setLayoutParams(cp);
        btnCollect.setOnClickListener(v -> collectWord());
        contentArea.addView(btnCollect);
    }

    private void addField(String label, String value) {
        TextView tv = new TextView(this);
        tv.setText(label + ": " + (value != null && !value.isEmpty() ? value : "-"));
        tv.setTextSize(16);
        tv.setTextColor(0xFF273245);
        tv.setPadding(12, 8, 12, 8);
        tv.setBackgroundColor(0xFFFFFFFF);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, 0, 8);
        tv.setLayoutParams(p);
        contentArea.addView(tv);
    }

    private void playAudio(String url) {
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(url);
            mp.prepareAsync();
            mp.setOnPreparedListener(MediaPlayer::start);
        } catch (Exception e) {
            Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void collectWord() {
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("wordId", wordId);
                JsonObject res = ApiClient.get().post("/api/collections/add", body);
                String msg = res.has("message") ? res.get("message").getAsString() : "操作失败";
                int code = res.has("code") ? res.get("code").getAsInt() : -1;
                runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showText(String msg) {
        contentArea.removeAllViews();
        TextView tv = new TextView(this);
        tv.setText(msg);
        tv.setTextSize(16);
        tv.setTextColor(0xFF8899aa);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, 40, 0, 40);
        contentArea.addView(tv);
    }
}
