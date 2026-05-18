package com.word.wordlearning.controller;

import com.word.wordlearning.dto.Result;
import com.word.wordlearning.entity.WordBook;
import com.word.wordlearning.service.WordBookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
