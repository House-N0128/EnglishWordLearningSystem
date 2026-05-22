package com.word.wordlearning.controller;

import com.word.wordlearning.dto.Result;
import com.word.wordlearning.entity.Word;
import com.word.wordlearning.mapper.WordBookMapper;
import com.word.wordlearning.mapper.WordMapper;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/words")
public class WordController {

    private final WordMapper wordMapper;
    private final WordBookMapper wordBookMapper;

    public WordController(WordMapper wordMapper, WordBookMapper wordBookMapper) {
        this.wordMapper = wordMapper;
        this.wordBookMapper = wordBookMapper;
    }

    @GetMapping
    public Result<List<Word>> list(@RequestParam("wordBookId") String wordBookId) {
        return Result.success(wordMapper.findByWordBookId(wordBookId));
    }

    @GetMapping("/{wordId}")
    public Result<Word> detail(@PathVariable String wordId) {
        Word word = wordMapper.findById(wordId);
        if (word == null) {
            return Result.error(404, "单词不存在");
        }
        return Result.success(word);
    }

    @GetMapping("/search")
    public Result<List<Word>> search(@RequestParam String keyword) {
        return Result.success(wordMapper.search(keyword));
    }

    @PostMapping
    public Result<String> add(@RequestBody Map<String, String> body) {
        String spelling = body.get("englishSpelling");
        String definition = body.get("chineseDefinition");
        if (spelling == null || definition == null) {
            return Result.error(400, "缺少必填字段");
        }

        // Check duplicate by spelling
        Word existBySpelling = wordMapper.findBySpelling(spelling);
        if (existBySpelling != null) {
            return Result.error(400, "单词已存在: " + spelling);
        }

        String wordId = body.get("wordId");
        if (wordId == null || wordId.isEmpty()) {
            wordId = "WD" + System.currentTimeMillis();
        }
        String bookId = body.get("wordBookId");
        String phonetic = body.get("phoneticSymbol") != null ? body.get("phoneticSymbol") : "";
        String example = body.get("exampleSentence") != null ? body.get("exampleSentence") : "";
        String audio = body.get("wordPronunciation") != null ? body.get("wordPronunciation") : "";
        String image = body.get("wordImage") != null ? body.get("wordImage") : "";

        Word exist = wordMapper.findById(wordId);
        if (exist == null) {
            Word w = new Word();
            w.setWordId(wordId);
            w.setEnglishSpelling(spelling);
            w.setChineseDefinition(definition);
            w.setPhoneticSymbol(phonetic);
            w.setExampleSentence(example);
            w.setWordPronunciation(audio);
            w.setWordImage(image);
            wordMapper.insert(w);
        }
        if (bookId != null && !bookId.isEmpty()) {
            wordMapper.insertBookRef(bookId, wordId);
            wordBookMapper.syncWordCount(bookId);
        }
        return Result.success("单词添加成功");
    }

    @PutMapping("/{wordId}")
    public Result<String> update(@PathVariable String wordId, @RequestBody Word word) {
        word.setWordId(wordId);
        wordMapper.update(word);
        return Result.success("单词修改成功");
    }

    @GetMapping("/all")
    public Result<List<Word>> listAll() {
        return Result.success(wordMapper.findAllWords());
    }

    @PostMapping("/batch")
    public Result<String> batchAdd(@RequestBody Map<String, Object> body) {
        String bookId = (String) body.get("wordBookId");
        if (bookId == null) return Result.error(400, "缺少词书ID");

        @SuppressWarnings("unchecked")
        List<Map<String, String>> words = (List<Map<String, String>>) body.get("words");
        if (words == null || words.isEmpty()) return Result.error(400, "单词列表为空");

        int success = 0, fail = 0, skipped = 0;
        int idSeq = 1;
        for (Map<String, String> w : words) {
            try {
                String spelling = w.get("englishSpelling");
                String definition = w.get("chineseDefinition");
                if (spelling == null || definition == null) { fail++; continue; }

                // Skip if already exists by spelling
                Word exist = wordMapper.findBySpelling(spelling);
                if (exist != null) { skipped++; continue; }

                // Auto-generate ID
                String wordId = w.get("wordId");
                if (wordId == null || wordId.isEmpty()) {
                    wordId = "WD" + System.currentTimeMillis() + String.format("%03d", idSeq++);
                }

                Word nw = new Word();
                nw.setWordId(wordId);
                nw.setEnglishSpelling(spelling);
                nw.setChineseDefinition(definition);
                nw.setPhoneticSymbol(w.getOrDefault("phoneticSymbol", ""));
                nw.setExampleSentence(w.getOrDefault("exampleSentence", ""));
                nw.setWordPronunciation(w.getOrDefault("wordPronunciation", ""));
                nw.setWordImage(w.getOrDefault("wordImage", ""));
                wordMapper.insert(nw);
                wordMapper.insertBookRef(bookId, wordId);
                success++;
            } catch (Exception e) { fail++; }
        }
        wordBookMapper.syncWordCount(bookId);
        String msg = "成功" + success + "个";
        if (skipped > 0) msg += "，跳过" + skipped + "个已存在";
        if (fail > 0) msg += "，失败" + fail + "个";
        return Result.success(msg);
    }

    @DeleteMapping("/{wordId}")
    public Result<String> delete(@PathVariable String wordId, @RequestParam(required = false) String wordBookId) {
        if (wordBookId != null) {
            // Delete only from this book
            int n = wordMapper.deleteBookRef(wordBookId, wordId);
            if (n > 0) {
                // If no more refs, delete the word itself
                if (wordMapper.countBookRefs(wordId) == 0) {
                    wordMapper.delete(wordId);
                }
                wordBookMapper.syncWordCount(wordBookId);
                return Result.success("已从词书移除");
            }
            return Result.error(404, "未找到关联");
        }
        // Full delete
        wordMapper.delete(wordId);
        return Result.success("单词已删除");
    }
}
