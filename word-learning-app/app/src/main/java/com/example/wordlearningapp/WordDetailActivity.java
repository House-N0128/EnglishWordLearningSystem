package com.example.wordlearningapp;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonObject;

public class WordDetailActivity extends AppCompatActivity {

    private static final String TAG = "WordDetailActivity";

    private TextView tvWordTitle;
    private TextView tvSpelling;
    private TextView tvPartOfSpeech;
    private TextView tvDefinition;
    private TextView tvPhonetic;
    private TextView tvExample;
    private Button btnAudio;
    private Button btnCollect;
    private ImageView ivWordImage;
    private TextView tvNoImage;

    private String wordId;
    private JsonObject currentWord;
    private MediaPlayer mediaPlayer;
    private boolean isFromCollection = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        wordId = getIntent().getStringExtra("wordId");
        isFromCollection = getIntent().getBooleanExtra("fromCollection", false);

        tvWordTitle = findViewById(R.id.tv_word_title);
        tvSpelling = findViewById(R.id.tv_spelling);
        tvPartOfSpeech = findViewById(R.id.tv_part_of_speech);
        tvDefinition = findViewById(R.id.tv_definition);
        tvPhonetic = findViewById(R.id.tv_phonetic);
        tvExample = findViewById(R.id.tv_example);
        btnAudio = findViewById(R.id.btn_audio);
        btnCollect = findViewById(R.id.btn_collect);
        ivWordImage = findViewById(R.id.iv_word_image);
        tvNoImage = findViewById(R.id.tv_no_image);

        ((TextView) findViewById(R.id.toolbar_title)).setText("单词详情");
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        if (wordId == null) {
            Toast.makeText(this, "缺少单词参数", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                Log.d(TAG, "加载单词详情: " + wordId);
                JsonObject res = ApiClient.get().get("/api/words/" + wordId);
                Log.d(TAG, "API响应: " + res.toString());

                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    currentWord = res.getAsJsonObject("data");
                    runOnUiThread(() -> buildUI(currentWord));
                } else {
                    String msg = res.has("message") ? res.get("message").getAsString() : "单词不存在";
                    runOnUiThread(() -> {
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "加载失败: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void buildUI(JsonObject w) {
        String spelling = w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "";
        String definition = w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "";
        String partOfSpeech = w.has("partOfSpeech") && !w.get("partOfSpeech").isJsonNull()
                ? w.get("partOfSpeech").getAsString() : "";
        String phonetic = w.has("phoneticSymbol") && !w.get("phoneticSymbol").isJsonNull()
                ? w.get("phoneticSymbol").getAsString() : "";
        String example = w.has("exampleSentence") && !w.get("exampleSentence").isJsonNull()
                ? w.get("exampleSentence").getAsString() : "";

        tvWordTitle.setText(spelling);
        tvSpelling.setText(spelling);
        tvPartOfSpeech.setText(partOfSpeech.isEmpty() ? "-" : partOfSpeech);
        tvDefinition.setText(definition);
        tvPhonetic.setText(phonetic.isEmpty() ? "-" : "/" + phonetic + "/");
        tvExample.setText(example.isEmpty() ? "-" : example);

        String audioUrl = w.has("wordPronunciation") && !w.get("wordPronunciation").isJsonNull()
                ? w.get("wordPronunciation").getAsString() : "";

        if (audioUrl != null && !audioUrl.isEmpty()) {
            btnAudio.setVisibility(Button.VISIBLE);
            btnAudio.setOnClickListener(v -> playAudio(audioUrl));
        } else {
            btnAudio.setVisibility(Button.GONE);
        }

        loadWordImage(spelling);

        if (isFromCollection) {
            btnCollect.setText("🗑️ 取消收藏");
            btnCollect.setBackgroundColor(0xFFe74c3c);
            btnCollect.setOnClickListener(v -> cancelCollection());
        } else {
            btnCollect.setText("⭐ 收藏单词");
            btnCollect.setBackgroundColor(0xFF039c83);
            btnCollect.setOnClickListener(v -> collectWord());
        }
    }

    private void loadWordImage(String spelling) {
        if (spelling == null || spelling.isEmpty()) {
            ivWordImage.setVisibility(ImageView.GONE);
            tvNoImage.setVisibility(TextView.VISIBLE);
            return;
        }

        try {
            String imageName = spelling.toLowerCase().trim();

            // 从mipmap目录查找图片
            // Android会根据设备密度自动选择合适的分辨率图片
            // 例如：mipmap/abandon/abandon.png (xhdpi)
            int imageResId = getResources().getIdentifier(imageName, "mipmap", getPackageName());

            Log.d(TAG, "查找图片: " + imageName + ", 资源ID: " + imageResId);

            if (imageResId != 0) {
                // 找到图片，显示图片，隐藏"暂无示意图"文字
                ivWordImage.setVisibility(ImageView.VISIBLE);
                tvNoImage.setVisibility(TextView.GONE);
                ivWordImage.setImageResource(imageResId);
                ivWordImage.setContentDescription(spelling + " - 示意图");
                Log.d(TAG, "成功加载图片: " + spelling);
            } else {
                // 未找到图片，隐藏图片，显示"暂无示意图"文字
                ivWordImage.setVisibility(ImageView.GONE);
                tvNoImage.setVisibility(TextView.VISIBLE);
                Log.d(TAG, "未找到图片: " + spelling + "，显示暂无示意图");
            }
        } catch (Exception e) {
            Log.e(TAG, "加载图片失败: " + e.getMessage());
            ivWordImage.setVisibility(ImageView.GONE);
            tvNoImage.setVisibility(TextView.VISIBLE);
        }
    }

    private void playAudio(String url) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                Toast.makeText(this, "播放中...", Toast.LENGTH_SHORT).show();
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                mediaPlayer = null;
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT).show();
                return true;
            });
        } catch (Exception e) {
            Log.e(TAG, "音频播放失败: " + e.getMessage());
            Toast.makeText(this, "播放失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void collectWord() {
        if (currentWord == null) {
            Toast.makeText(this, "单词数据未加载", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("wordId", wordId);

                Log.d(TAG, "收藏单词: " + wordId);
                JsonObject res = ApiClient.get().post("/api/collections/add", body);
                Log.d(TAG, "收藏响应: " + res.toString());

                String msg = res.has("message") ? res.get("message").getAsString() : "操作失败";
                int code = res.has("code") ? res.get("code").getAsInt() : -1;

                runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Log.e(TAG, "收藏失败: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "网络错误: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void cancelCollection() {
        if (currentWord == null) {
            Toast.makeText(this, "单词数据未加载", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("确认取消")
                .setMessage("确定取消收藏该单词？")
                .setPositiveButton("确定", (d, w) -> {
                    new Thread(() -> {
                        try {
                            JsonObject body = new JsonObject();
                            body.addProperty("wordId", wordId);

                            Log.d(TAG, "取消收藏: " + wordId);
                            JsonObject res = ApiClient.get().delete("/api/collections/remove", body);
                            Log.d(TAG, "取消收藏响应: " + res.toString());

                            String msg = res.has("message") ? res.get("message").getAsString() : "操作失败";
                            int code = res.has("code") ? res.get("code").getAsInt() : -1;

                            runOnUiThread(() -> {
                                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                                if (code == 200) {
                                    Log.d(TAG, "取消收藏成功，返回收藏页面");
                                    finish();
                                }
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "取消收藏失败: " + e.getMessage(), e);
                            runOnUiThread(() -> Toast.makeText(this, "网络错误: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                })
                .setNegativeButton("取消", null)
                .create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
