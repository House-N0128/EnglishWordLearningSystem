package com.word.wordlearning.service;

import com.word.wordlearning.dto.LoginRequest;
import com.word.wordlearning.dto.LoginResponse;
import com.word.wordlearning.entity.Adminstrator;
import com.word.wordlearning.mapper.AdminstratorMapper;
import org.springframework.stereotype.Service;

@Service
public class AdminstratorService {

    private final AdminstratorMapper adminstratorMapper;

    public AdminstratorService(AdminstratorMapper adminstratorMapper) {
        this.adminstratorMapper = adminstratorMapper;
    }

    public LoginResponse login(LoginRequest request) {
        Adminstrator admin = adminstratorMapper.findByUserIdAndPassword(
                request.getUserid(), request.getPassword());

        if (admin == null) {
            return new LoginResponse(request.getUserid(), null, null,
                    "用户名或密码错误");
        }

        if ("冻结".equals(admin.getAccountStatus())) {
            return new LoginResponse(admin.getUserId(), "管理员",
                    admin.getAccountStatus(), "账号已被禁用");
        }

        return new LoginResponse(admin.getUserId(), "管理员",
                admin.getAccountStatus(), "登录成功");
    }
}
