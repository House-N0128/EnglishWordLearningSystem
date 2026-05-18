package com.word.wordlearning.controller;

import com.word.wordlearning.dto.Result;
import com.word.wordlearning.entity.OrdinaryUser;
import com.word.wordlearning.mapper.OrdinaryUserMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final OrdinaryUserMapper userMapper;

    public UserController(OrdinaryUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody OrdinaryUser user) {
        OrdinaryUser exist = userMapper.findByUserId(user.getUserId());
        if (exist != null) {
            return Result.error(400, "账号已存在");
        }
        userMapper.insert(user);
        return Result.success("注册成功");
    }

    @GetMapping("/profile")
    public Result<OrdinaryUser> profile(@RequestHeader("X-User-Id") String userId) {
        OrdinaryUser user = userMapper.findByUserId(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setLoginPassword(null);
        return Result.success(user);
    }

    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestHeader("X-User-Id") String userId,
                                        @RequestBody OrdinaryUser updated) {
        updated.setUserId(userId);
        userMapper.updateProfile(updated);
        return Result.success("更新成功");
    }

    @PutMapping("/password")
    public Result<String> updatePassword(@RequestHeader("X-User-Id") String userId,
                                         @RequestBody java.util.Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        OrdinaryUser user = userMapper.findByUserIdAndPassword(userId, oldPassword);
        if (user == null) {
            return Result.error(400, "当前密码错误");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Result.error(400, "新密码长度至少6位");
        }
        userMapper.updatePassword(userId, newPassword);
        return Result.success("密码修改成功");
    }

    @DeleteMapping("/account")
    public Result<String> deleteAccount(@RequestHeader("X-User-Id") String userId,
                                        @RequestBody java.util.Map<String, String> body) {
        OrdinaryUser user = userMapper.findByUserIdAndPassword(userId, body.get("password"));
        if (user == null) {
            return Result.error(400, "密码错误");
        }
        userMapper.deleteByUserId(userId);
        return Result.success("账号已注销");
    }
}
