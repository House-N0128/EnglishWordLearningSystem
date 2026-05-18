package com.word.wordlearning.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String userid;
    private String nickname;
    private String accountStatus;
    private String message;
}
