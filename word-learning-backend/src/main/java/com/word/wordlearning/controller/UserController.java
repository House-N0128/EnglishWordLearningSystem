package com.word.wordlearning.controller;

import com.word.wordlearning.dto.Result;
import com.word.wordlearning.entity.OrdinaryUser;
import com.word.wordlearning.mapper.OrdinaryUserMapper;
import com.word.wordlearning.service.VerificationCodeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final OrdinaryUserMapper userMapper;
    private final VerificationCodeService verificationCodeService;

    public UserController(OrdinaryUserMapper userMapper,
                          VerificationCodeService verificationCodeService) {
        this.userMapper = userMapper;
        this.verificationCodeService = verificationCodeService;
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

    @PostMapping("/forgot-password")
    public Result<String> forgotPassword(@RequestBody java.util.Map<String, String> body) {
        String userId = body.get("userId");
        String phone = body.get("phoneNumber");
        String newPassword = body.get("newPassword");
        if (userId == null || phone == null || newPassword == null) {
            return Result.error(400, "请填写账号、手机号和新密码");
        }
        if (newPassword.length() < 6) {
            return Result.error(400, "新密码长度至少6位");
        }
        OrdinaryUser user = userMapper.findByUserId(userId);
        if (user == null) {
            return Result.error(404, "账号不存在");
        }
        if (!phone.equals(user.getPhoneNumber())) {
            return Result.error(400, "手机号与注册时不一致");
        }
        userMapper.updatePassword(userId, newPassword);
        return Result.success("密码重置成功，请登录");
    }

    @PostMapping("/send-verification-code")
    public Result<String> sendVerificationCode(@RequestBody java.util.Map<String, String> body) {
        String contact = body.get("contact");
        if (contact == null || contact.trim().isEmpty()) {
            return Result.error(400, "请输入手机号或邮箱");
        }
        contact = contact.trim();
        String error = verificationCodeService.sendCode(contact);
        if (error != null) {
            return Result.error(400, error);
        }
        return Result.success("验证码已发送");
    }

    @PostMapping("/verify-code")
    public Result<String> verifyCode(@RequestBody java.util.Map<String, String> body) {
        String contact = body.get("contact");
        String code = body.get("verifyCode");
        if (contact == null || contact.trim().isEmpty()) {
            return Result.error(400, "参数错误");
        }
        if (code == null || code.trim().isEmpty()) {
            return Result.error(400, "请输入验证码");
        }
        contact = contact.trim();
        String error = verificationCodeService.verifyCode(contact, code.trim());
        if (error != null) {
            return Result.error(400, error);
        }
        return Result.success("验证通过");
    }

    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody java.util.Map<String, String> body) {
        String contact = body.get("contact");
        String newPassword = body.get("newPassword");
        if (contact == null || contact.trim().isEmpty()) {
            return Result.error(400, "参数错误，缺少联系方式");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Result.error(400, "新密码长度至少6位");
        }
        contact = contact.trim();
        int rows = userMapper.resetPasswordByContact(contact, newPassword);
        if (rows == 0) {
            return Result.error(404, "未找到该用户");
        }
        return Result.success("密码重置成功，请前往登录");
    }
}
