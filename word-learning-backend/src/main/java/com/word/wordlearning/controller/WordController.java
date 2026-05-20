package com.word.wordlearning.controller;

import com.word.wordlearning.dto.Result;
import com.word.wordlearning.entity.Word;
import com.word.wordlearning.mapper.WordMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/words")
public class WordController {

    private final WordMapper wordMapper;

    public WordController(WordMapper wordMapper) {
        this.wordMapper = wordMapper;
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
        String wordId = body.get("wordId");
        String bookId = body.get("wordBookId");
        String spelling = body.get("englishSpelling");
        String definition = body.get("chineseDefinition");
        String phonetic = body.get("phoneticSymbol") != null ? body.get("phoneticSymbol") : "";
        String example = body.get("exampleSentence") != null ? body.get("exampleSentence") : "";
        String audio = body.get("wordPronunciation") != null ? body.get("wordPronunciation") : "";
        String image = body.get("wordImage") != null ? body.get("wordImage") : "";

        if (wordId == null || spelling == null || definition == null || bookId == null) {
            return Result.error(400, "缺少必填字段");
        }

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
        wordMapper.insertBookRef(bookId, wordId);
        return Result.success("单词添加成功");
    }

    @PutMapping("/{wordId}")
    public Result<String> update(@PathVariable String wordId, @RequestBody Word word) {
        word.setWordId(wordId);
        wordMapper.update(word);
        return Result.success("单词修改成功");
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
                return Result.success("已从词书移除");
            }
            return Result.error(404, "未找到关联");
        }
        // Full delete
        wordMapper.delete(wordId);
        return Result.success("单词已删除");
    }
}
