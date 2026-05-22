package com.word.wordlearning.controller;

import com.word.wordlearning.dto.Result;
import com.word.wordlearning.entity.WordBook;
import com.word.wordlearning.mapper.WordBookMapper;
import com.word.wordlearning.service.WordBookService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wordbooks")
public class WordBookController {

    private final WordBookService wordBookService;
    private final WordBookMapper wordBookMapper;

    public WordBookController(WordBookService wordBookService, WordBookMapper wordBookMapper) {
        this.wordBookService = wordBookService;
        this.wordBookMapper = wordBookMapper;
    }

    @GetMapping
    public Result<List<WordBook>> list() {
        return Result.success(wordBookService.listAvailable());
    }

    @PostMapping
    public Result<String> add(@RequestBody WordBook book) {
        // Check duplicate name
        if (book.getWordBookName() != null) {
            WordBook dup = wordBookMapper.findByName(book.getWordBookName());
            if (dup != null) return Result.error(400, "词书名称已存在");
        }
        // Auto-generate ID: WB001, WB002...
        String maxId = wordBookMapper.maxBookId();
        int next = 1;
        if (maxId != null && maxId.startsWith("WB")) {
            try { next = Integer.parseInt(maxId.substring(2)) + 1; } catch (Exception e) {}
        }
        book.setWordBookId("WB" + String.format("%03d", next));
        wordBookService.add(book);
        return Result.success("词书添加成功");
    }

    @PutMapping("/{wordBookId}")
    public Result<String> update(@PathVariable String wordBookId, @RequestBody WordBook book) {
        book.setWordBookId(wordBookId);
        wordBookService.update(book);
        return Result.success("词书修改成功");
    }

    @DeleteMapping("/{wordBookId}")
    public Result<String> delete(@PathVariable String wordBookId) {
        wordBookService.delete(wordBookId);
        return Result.success("词书已删除");
    }
}
