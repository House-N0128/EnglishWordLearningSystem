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
        String bookId = body.get("wordBookId");
        if (spelling == null || definition == null) {
            return Result.error(400, "缺少必填字段");
        }

        // Find existing word by spelling
        Word existBySpelling = wordMapper.findBySpelling(spelling);

        // If adding to a specific book
        if (bookId != null && !bookId.isEmpty()) {
            if (existBySpelling != null) {
                // Word exists - check if already in this book
                if (wordMapper.existsBookRef(bookId, existBySpelling.getWordId()) > 0) {
                    return Result.error(400, "该单词已在此词书中");
                }
                // Word exists but not in this book - add ref
                wordMapper.insertBookRef(bookId, existBySpelling.getWordId());
                wordBookMapper.syncWordCount(bookId);
                return Result.success("已将单词添加到词书");
            }
            // Word doesn't exist - create it and add ref
            String newId = body.get("wordId");
            if (newId == null || newId.isEmpty()) {
                newId = generateWordId();
            }
            Word w = new Word();
            w.setWordId(newId);
            w.setEnglishSpelling(spelling);
            w.setChineseDefinition(definition);
            w.setPhoneticSymbol(body.get("phoneticSymbol") != null ? body.get("phoneticSymbol") : "");
            w.setPartOfSpeech(body.get("partOfSpeech") != null ? body.get("partOfSpeech") : "");
            w.setExampleSentence(body.get("exampleSentence") != null ? body.get("exampleSentence") : "");
            w.setWordPronunciation(body.get("wordPronunciation") != null ? body.get("wordPronunciation") : "");
            w.setWordImage(body.get("wordImage") != null ? body.get("wordImage") : "");
            wordMapper.insert(w);
            wordMapper.insertBookRef(bookId, newId);
            wordBookMapper.syncWordCount(bookId);
            return Result.success("单词已创建并添加到词书");
        }

        // No book specified - just create word (prevent duplicate)
        if (existBySpelling != null) {
            return Result.error(400, "单词已存在: " + spelling);
        }

        String wordId = body.get("wordId");
        if (wordId == null || wordId.isEmpty()) {
            wordId = generateWordId();
        }
        String phonetic = body.get("phoneticSymbol") != null ? body.get("phoneticSymbol") : "";
        String partOfSpeech = body.get("partOfSpeech") != null ? body.get("partOfSpeech") : "";
        String example = body.get("exampleSentence") != null ? body.get("exampleSentence") : "";
        String audio = body.get("wordPronunciation") != null ? body.get("wordPronunciation") : "";
        String image = body.get("wordImage") != null ? body.get("wordImage") : "";

        Word w = new Word();
        w.setWordId(wordId);
        w.setEnglishSpelling(spelling);
        w.setChineseDefinition(definition);
        w.setPhoneticSymbol(phonetic);
        w.setPartOfSpeech(partOfSpeech);
        w.setExampleSentence(example);
        w.setWordPronunciation(audio);
        w.setWordImage(image);
        wordMapper.insert(w);
        return Result.success("单词添加成功");
    }

    private String generateWordId() {
        Integer max = wordMapper.maxWordIdNum();
        int next = (max == null) ? 1 : max + 1;
        return "WD" + String.format("%05d", next);
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
        Integer maxNum = wordMapper.maxWordIdNum();
        int idSeq = (maxNum == null) ? 1 : maxNum + 1;
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
                    wordId = "WD" + String.format("%05d", idSeq++);
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
