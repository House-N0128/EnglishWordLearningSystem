package com.example.wordlearningapp;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class WordStudyActivity extends AppCompatActivity {

    private TextView tvBack, tvStudyTitle, tvWord, tvPhonetic, tvPos, tvChinese, tvExample, tvProgress;
    private ImageView ivAudio;
    private Button btnCollect, btnPrev, btnNext, btnMastered, btnFinish;
    private ProgressBar progressBar;
    private String bookId;
    private String bookName;
    private JsonArray words;
    private int currentIndex = 0;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_study);

        bookId = getIntent().getStringExtra("bookId");
        bookName = getIntent().getStringExtra("bookName");

        initViews();
        initListeners();

        if (bookId == null) {
            Toast.makeText(this, "缺少词书参数", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (bookName != null) {
            tvStudyTitle.setText("学习 - " + bookName);
        }

        loadWords();
    }

    private void initViews() {
        tvBack = findViewById(R.id.tv_back);
        tvStudyTitle = findViewById(R.id.tv_study_title);
        tvWord = findViewById(R.id.tv_word);
        tvPhonetic = findViewById(R.id.tv_phonetic);
        tvPos = findViewById(R.id.tv_pos);
        tvChinese = findViewById(R.id.tv_chinese);
        tvExample = findViewById(R.id.tv_example);
        tvProgress = findViewById(R.id.tv_progress);
        ivAudio = findViewById(R.id.iv_audio);
        btnCollect = findViewById(R.id.btn_collect);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        btnMastered = findViewById(R.id.btn_mastered);
        btnFinish = findViewById(R.id.btn_finish);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void initListeners() {
        tvBack.setOnClickListener(v -> finish());

        ivAudio.setOnClickListener(v -> playAudio());

        btnCollect.setOnClickListener(v -> {
            String wordId = getCurrentWordId();
            if (wordId != null) {
                collectWord(wordId);
            }
        });

        btnPrev.setOnClickListener(v -> showWord(currentIndex - 1));

        btnNext.setOnClickListener(v -> showWord(currentIndex + 1));

        btnMastered.setOnClickListener(v -> {
            String wordId = getCurrentWordId();
            if (wordId != null) {
                markLearned(wordId);
            }
        });

        btnFinish.setOnClickListener(v -> finish());
    }

    private void loadWords() {
        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/words?wordBookId=" + bookId);
                if (res.get("code").getAsInt() == 200) {
                    words = res.getAsJsonArray("data");
                    runOnUiThread(() -> {
                        if (words.size() == 0) {
                            Toast.makeText(this, "该词书暂无单词", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            showWord(0);
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "加载失败", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void showWord(int index) {
        if (words == null || words.size() == 0) return;
        if (index < 0) index = 0;
        if (index >= words.size()) index = words.size() - 1;
        currentIndex = index;

        JsonObject w = words.get(index).getAsJsonObject();

        String spelling = w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "";
        String phonetic = (w.has("phoneticSymbol") && !w.get("phoneticSymbol").isJsonNull())
                ? w.get("phoneticSymbol").getAsString() : "";
        String chinese = w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "";
        String pos = w.has("partOfSpeech") && !w.get("partOfSpeech").isJsonNull()
                ? w.get("partOfSpeech").getAsString() : "";
        String example = (w.has("exampleSentence") && !w.get("exampleSentence").isJsonNull())
                ? w.get("exampleSentence").getAsString() : "";

        tvWord.setText(spelling);
        tvPhonetic.setText(phonetic.isEmpty() ? "" : "[" + phonetic + "]");
        tvPos.setText(pos);
        tvChinese.setText(chinese);

        if (!example.isEmpty()) {
            tvExample.setText("例句：" + example);
            tvExample.setVisibility(TextView.VISIBLE);
        } else {
            tvExample.setVisibility(TextView.GONE);
        }

        int progress = (int) ((currentIndex + 1) * 100.0 / words.size());
        progressBar.setProgress(progress);
        tvProgress.setText("已学 " + (currentIndex + 1) + " / 总 " + words.size() + " 单词");
    }

    private void playAudio() {
        if (words == null || currentIndex >= words.size()) return;

        JsonObject w = words.get(currentIndex).getAsJsonObject();

        // 尝试从后端获取音频URL（字段名可能是 audioUrl 或 wordPronunciation）
        String audioUrl = "";
        if (w.has("audioUrl") && !w.get("audioUrl").isJsonNull()) {
            audioUrl = w.get("audioUrl").getAsString();
        } else if (w.has("wordPronunciation") && !w.get("wordPronunciation").isJsonNull()) {
            audioUrl = w.get("wordPronunciation").getAsString();
        }

        // 如果后端没有提供音频URL，使用在线词典API
        if (audioUrl.isEmpty()) {
            String spelling = w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "";
            if (!spelling.isEmpty()) {
                // 使用有道词典的在线音频API
                audioUrl = "https://dict.youdao.com/dictvoice?audio=" + spelling + "&type=1";
                Toast.makeText(this, "正在播放...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "暂无音频", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioUrl);
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
                Toast.makeText(this, "音频播放失败", Toast.LENGTH_SHORT).show();
                mp.release();
                mediaPlayer = null;
                return true;
            });
        } catch (Exception e) {
            Toast.makeText(this, "音频播放失败", Toast.LENGTH_SHORT).show();
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }

    private String getCurrentWordId() {
        if (words != null && currentIndex < words.size()) {
            JsonObject w = words.get(currentIndex).getAsJsonObject();
            return w.has("wordId") ? w.get("wordId").getAsString() : null;
        }
        return null;
    }

    private void collectWord(String wordId) {
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("wordId", wordId);
                JsonObject res = ApiClient.get().post("/api/collections/add", body);
                String msg = res.has("message") ? res.get("message").getAsString() : "操作失败";
                runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void markLearned(String wordId) {
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("wordId", wordId);
                body.addProperty("learnedWordBookId", bookId);
                JsonObject res = ApiClient.get().post("/api/records/add", body);
                String msg = res.has("message") ? res.get("message").getAsString() : "操作失败";
                runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show());
            }
        }).start();
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
