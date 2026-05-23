package com.example.wordlearningapp;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.WordImageLoader;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordStudyActivity extends AppCompatActivity {

    private static final String TAG = "WordStudyActivity";

    private TextView tvBack, tvStudyTitle, tvWord, tvPhonetic, tvPos, tvChinese, tvExample, tvProgress, tvNoImage;
    private ImageView ivAudio, ivWordImage;
    private Button btnCollect, btnPrev, btnNext, btnMastered, btnFinish;
    private ProgressBar progressBar;
    private String bookId;
    private String bookName;
    private String studyMode;
    private JsonArray words;
    private int currentIndex = 0;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_study);

        bookId = getIntent().getStringExtra("bookId");
        bookName = getIntent().getStringExtra("bookName");
        studyMode = getIntent().getStringExtra("studyMode");

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
        tvNoImage = findViewById(R.id.tv_no_image);
        ivAudio = findViewById(R.id.iv_audio);
        ivWordImage = findViewById(R.id.iv_word_image);
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

        btnPrev.setOnClickListener(v -> {
            if (currentIndex > 0) {
                loadWordDetailsAndShow(currentIndex - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (words != null && currentIndex < words.size() - 1) {
                loadWordDetailsAndShow(currentIndex + 1);
            }
        });

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
                JsonObject wordsRes = ApiClient.get().get("/api/words?wordBookId=" + bookId);

                if (wordsRes.get("code").getAsInt() != 200) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "加载单词失败", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                JsonArray allWords = wordsRes.getAsJsonArray("data");

                if ("continue".equals(studyMode)) {
                    JsonObject recordsRes = ApiClient.get().get("/api/records/list");
                    Set<String> learnedWordIds = new HashSet<>();

                    if (recordsRes.get("code").getAsInt() == 200) {
                        JsonArray records = recordsRes.getAsJsonArray("data");

                        for (int i = 0; i < records.size(); i++) {
                            JsonObject record = records.get(i).getAsJsonObject();

                            String recordBookId = "";
                            if (record.has("learnedWordBookId")) {
                                recordBookId = record.get("learnedWordBookId").getAsString();
                            } else if (record.has("wordBookId")) {
                                recordBookId = record.get("wordBookId").getAsString();
                            }

                            if (bookId.equals(recordBookId)) {
                                String wordId = "";
                                if (record.has("learnedWordId")) {
                                    wordId = record.get("learnedWordId").getAsString();
                                } else if (record.has("wordId")) {
                                    wordId = record.get("wordId").getAsString();
                                } else if (record.has("id")) {
                                    wordId = record.get("id").getAsString();
                                }

                                if (!wordId.isEmpty()) {
                                    learnedWordIds.add(wordId);
                                }
                            }
                        }
                    }

                    List<JsonObject> unlearnedWords = new ArrayList<>();
                    for (int i = 0; i < allWords.size(); i++) {
                        JsonObject word = allWords.get(i).getAsJsonObject();
                        String wordId = word.has("wordId") ? word.get("wordId").getAsString() : "";

                        if (!wordId.isEmpty() && !learnedWordIds.contains(wordId)) {
                            unlearnedWords.add(word);
                        }
                    }

                    runOnUiThread(() -> {
                        if (unlearnedWords.isEmpty()) {
                            Toast.makeText(this, "恭喜！该词书已全部学完", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            words = new JsonArray();
                            for (JsonObject word : unlearnedWords) {
                                words.add(word);
                            }
                            loadWordDetailsAndShow(0);
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        if (allWords.size() == 0) {
                            Toast.makeText(this, "该词书暂无单词", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            words = allWords;
                            loadWordDetailsAndShow(0);
                        }
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

    /**
     * 加载单词详情并显示（参考WordDetailActivity的实现）
     */
    private void loadWordDetailsAndShow(int index) {
        if (words == null || words.size() == 0) return;
        if (index < 0) index = 0;
        if (index >= words.size()) index = words.size() - 1;

        final int currentIndex = index;

        JsonObject basicWord = words.get(currentIndex).getAsJsonObject();
        String wordId = basicWord.has("wordId") ? basicWord.get("wordId").getAsString() : "";

        if (wordId.isEmpty()) {
            showWord(currentIndex);
            return;
        }

        // 调用单个单词详情接口获取完整信息（包括词性和图片）
        new Thread(() -> {
            try {
                Log.d(TAG, "加载单词详情: " + wordId);
                JsonObject res = ApiClient.get().get("/api/words/" + wordId);
                Log.d(TAG, "详情API响应: " + res.toString());

                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    JsonObject detailedWord = res.getAsJsonObject("data");
                    runOnUiThread(() -> {
                        // 用详情替换基础信息
                        words.set(currentIndex, detailedWord);
                        showWord(currentIndex);
                    });
                } else {
                    runOnUiThread(() -> showWord(currentIndex));
                }
            } catch (Exception e) {
                Log.e(TAG, "加载详情失败: " + e.getMessage(), e);
                runOnUiThread(() -> showWord(currentIndex));
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
        String pos = (w.has("partOfSpeech") && !w.get("partOfSpeech").isJsonNull())
                ? w.get("partOfSpeech").getAsString() : "";
        String example = (w.has("exampleSentence") && !w.get("exampleSentence").isJsonNull())
                ? w.get("exampleSentence").getAsString() : "";
        String wordImage = (w.has("wordImage") && !w.get("wordImage").isJsonNull())
                ? w.get("wordImage").getAsString() : "";

        Log.d(TAG, "=== 单词详情 ===");
        Log.d(TAG, "拼写: " + spelling);
        Log.d(TAG, "词性: " + (pos.isEmpty() ? "空" : pos));
        Log.d(TAG, "释义: " + chinese);
        Log.d(TAG, "图片路径: " + (wordImage.isEmpty() ? "空" : wordImage));

        tvWord.setText(spelling);
        tvPhonetic.setText(phonetic.isEmpty() ? "-" : "[" + phonetic + "]");
        tvPos.setText(pos.isEmpty() ? "-" : pos);
        tvChinese.setText(chinese.isEmpty() ? "-" : chinese);

        if (!example.isEmpty()) {
            tvExample.setText(example);
            tvExample.setVisibility(TextView.VISIBLE);
        } else {
            tvExample.setVisibility(TextView.GONE);
        }

        loadWordImage(w);

        int progress = (int) ((currentIndex + 1) * 100.0 / words.size());
        progressBar.setProgress(progress);

        if ("continue".equals(studyMode)) {
            tvProgress.setText("第 " + (currentIndex + 1) + " 个 / 剩余 " + words.size() + " 个单词");
        } else {
            tvProgress.setText("第 " + (currentIndex + 1) + " 个 / 总 " + words.size() + " 个单词");
        }
    }

    /**
     * 加载单词示意图（完全参考WordDetailActivity的实现）
     */
    private void loadWordImage(JsonObject w) {
        String wordImageUrl = null;
        if (w.has("wordImage")) {
            if (!w.get("wordImage").isJsonNull()) {
                wordImageUrl = w.get("wordImage").getAsString();
            }
        }

        Log.d(TAG, "=== 图片加载信息 ===");
        Log.d(TAG, "wordImage字段存在: " + w.has("wordImage"));
        Log.d(TAG, "wordImage是否为null: " + (w.has("wordImage") ? w.get("wordImage").isJsonNull() : "N/A"));
        Log.d(TAG, "wordImage值: " + (wordImageUrl == null ? "null" : wordImageUrl));
        Log.d(TAG, "完整JSON: " + w.toString());

        WordImageLoader.loadWordImage(ivWordImage, wordImageUrl, tvNoImage);
    }

    private void playAudio() {
        if (words == null || currentIndex >= words.size()) return;

        JsonObject w = words.get(currentIndex).getAsJsonObject();

        String audioUrl = "";
        if (w.has("audioUrl") && !w.get("audioUrl").isJsonNull()) {
            audioUrl = w.get("audioUrl").getAsString();
        } else if (w.has("wordPronunciation") && !w.get("wordPronunciation").isJsonNull()) {
            audioUrl = w.get("wordPronunciation").getAsString();
        }

        if (audioUrl.isEmpty()) {
            String spelling = w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "";
            if (!spelling.isEmpty()) {
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
            Log.e(TAG, "音频播放失败: " + e.getMessage(), e);
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
                Log.e(TAG, "收藏失败: " + e.getMessage(), e);
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
                Log.e(TAG, "标记失败: " + e.getMessage(), e);
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
