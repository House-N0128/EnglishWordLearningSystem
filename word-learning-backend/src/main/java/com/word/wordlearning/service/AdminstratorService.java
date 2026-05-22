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

    public boolean updateAdminName(String oldUserId, String newUserId) {
        // 如果新用户名和旧用户名相同，直接返回成功
        if (oldUserId.equals(newUserId)) {
            return true;
        }
        // 检查新用户名是否已存在
        Adminstrator existingAdmin = adminstratorMapper.findByUserId(newUserId);
        if (existingAdmin != null) {
            return false; // 用户名已存在
        }
        // 更新用户名
        int affectedRows = adminstratorMapper.updateUserId(oldUserId, newUserId);
        return affectedRows > 0;
    }

    public Adminstrator getAdminInfo(String userId) {
        return adminstratorMapper.findByUserId(userId);
    }

    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        // 先验证原密码
        Adminstrator admin = adminstratorMapper.findByUserIdAndPassword(userId, oldPassword);
        if (admin == null) {
            return false; // 原密码错误
        }
        // 更新密码
        adminstratorMapper.updatePassword(userId, newPassword);
        return true;
    }
}