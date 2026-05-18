package com.word.wordlearning.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrdinaryUser {
    private String userId;
    private String loginPassword;
    private String accountStatus;
    private LocalDateTime registerTime;
    private LocalDateTime lastLoginTime;
    private String userName;
    private String phoneNumber;
    private String email;
}
