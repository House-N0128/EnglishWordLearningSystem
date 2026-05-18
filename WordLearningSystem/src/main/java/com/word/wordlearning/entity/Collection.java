package com.word.wordlearning.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Collection {
    private String collectionId;
    private String userId;
    private String wordId;
    private LocalDateTime collectionTime;
    private String englishSpelling;
    private String chineseDefinition;
    private String phoneticSymbol;
}
