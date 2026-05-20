package com.word.wordlearning.entity;

import lombok.Data;

@Data
public class WordBookRef {
    private Integer id;
    private String wordBookId;
    private String wordId;
    private Integer sortOrder;
}
