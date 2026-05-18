package com.word.wordlearning.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WordLearningRecord {
    private String recordId;
    private String userId;
    private String learnedWordBookId;
    private String wordId;
    private LocalDate learningDate;
    private LocalDateTime recordCreateTime;
}
