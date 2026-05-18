package com.word.wordlearning.controller;

import com.word.wordlearning.dto.Result;
import com.word.wordlearning.entity.Word;
import com.word.wordlearning.mapper.WordMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
