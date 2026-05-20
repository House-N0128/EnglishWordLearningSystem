package com.word.wordlearning.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Word {
    private String wordId;
    private String englishSpelling;
    private String chineseDefinition;
    private String exampleSentence;
    private String phoneticSymbol;
    private String wordPronunciation;
    private String wordImage;
    private LocalDateTime createTime;
}
