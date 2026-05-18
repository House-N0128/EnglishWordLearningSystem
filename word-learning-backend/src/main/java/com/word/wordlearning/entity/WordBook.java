package com.word.wordlearning.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WordBook {
    private String wordBookId;
    private String wordBookName;
    private String difficultyLevel;
    private String wordBookDescription;
    private Integer wordCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String wordBookStatus;
}
