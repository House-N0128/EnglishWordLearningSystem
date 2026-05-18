package com.word.wordlearning.service;

import com.word.wordlearning.dto.LoginRequest;
import com.word.wordlearning.dto.LoginResponse;
import com.word.wordlearning.entity.OrdinaryUser;
import com.word.wordlearning.mapper.OrdinaryUserMapper;
import org.springframework.stereotype.Service;

@Service
public class OrdinaryUserService {

    private final OrdinaryUserMapper ordinaryUserMapper;

    public OrdinaryUserService(OrdinaryUserMapper ordinaryUserMapper) {
        this.ordinaryUserMapper = ordinaryUserMapper;
    }

    public LoginResponse login(LoginRequest request) {
        OrdinaryUser user = ordinaryUserMapper.findByUserIdAndPassword(
                request.getUserid(), request.getPassword());

        if (user == null) {
            return new LoginResponse(request.getUserid(), null, null,
                    "用户名或密码错误");
        }

        if ("冻结".equals(user.getAccountStatus())) {
            return new LoginResponse(user.getUserId(), user.getUserName(),
                    user.getAccountStatus(), "账号已被禁用");
        }

        return new LoginResponse(user.getUserId(), user.getUserName(),
                user.getAccountStatus(), "登录成功");
    }
}
