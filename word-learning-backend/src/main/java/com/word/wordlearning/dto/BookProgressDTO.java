package com.word.wordlearning.dto;

import lombok.Data;

@Data
public class BookProgressDTO {
    private String wordBookId;
    private String wordBookName;
    private int totalWords;
    private int learnedWords;
}
