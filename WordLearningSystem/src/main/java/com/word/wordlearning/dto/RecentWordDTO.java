package com.word.wordlearning.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecentWordDTO {
    private String englishSpelling;
    private String chineseDefinition;
}
