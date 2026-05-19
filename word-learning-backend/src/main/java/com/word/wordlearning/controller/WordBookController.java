package com.word.wordlearning.controller;

import com.word.wordlearning.dto.Result;
import com.word.wordlearning.entity.WordBook;
import com.word.wordlearning.service.WordBookService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wordbooks")
public class WordBookController {

    private final WordBookService wordBookService;

    public WordBookController(WordBookService wordBookService) {
        this.wordBookService = wordBookService;
    }

    @GetMapping
    public Result<List<WordBook>> list() {
        return Result.success(wordBookService.listAvailable());
    }

    @PostMapping
    public Result<String> add(@RequestBody WordBook book) {
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
        return Result.success("词书已下架");
    }
}
