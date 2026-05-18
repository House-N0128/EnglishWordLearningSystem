package com.word.wordlearning.dto;

import lombok.Data;

@Data
public class AdminStatsDTO {
    private int userCount;
    private int wordBookCount;
    private int wordCount;
    private int todayRecordCount;
}
