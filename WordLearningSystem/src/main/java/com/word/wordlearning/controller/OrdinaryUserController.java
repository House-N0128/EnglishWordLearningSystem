package com.word.wordlearning.controller;

import com.word.wordlearning.dto.LoginRequest;
import com.word.wordlearning.dto.LoginResponse;
import com.word.wordlearning.dto.Result;
import com.word.wordlearning.service.OrdinaryUserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class OrdinaryUserController {

    private final OrdinaryUserService ordinaryUserService;

    public OrdinaryUserController(OrdinaryUserService ordinaryUserService) {
        this.ordinaryUserService = ordinaryUserService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = ordinaryUserService.login(request);
        if ("登录成功".equals(response.getMessage())) {
            return Result.success(response);
        }
        return Result.error(401, response.getMessage());
    }
}
