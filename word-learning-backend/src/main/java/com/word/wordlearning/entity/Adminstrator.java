package com.word.wordlearning.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Adminstrator {
    private String userId;
    private String loginPassword;
    private String phoneNumber;
    private String email;
    private String accountStatus;
    private LocalDateTime createTime;
}
