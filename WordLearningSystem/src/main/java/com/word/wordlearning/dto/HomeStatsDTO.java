package com.word.wordlearning.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HomeStatsDTO {
    private int todayCount;
    private int totalCount;
    private String currentBookName;
}
