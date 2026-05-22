package com.word.wordlearning.controller;

import com.word.wordlearning.dto.AdminStatsDTO;
import com.word.wordlearning.dto.LoginRequest;
import com.word.wordlearning.dto.LoginResponse;
import com.word.wordlearning.dto.Result;
import com.word.wordlearning.entity.OrdinaryUser;
import com.word.wordlearning.entity.WordBook;
import com.word.wordlearning.entity.WordLearningRecord;
import com.word.wordlearning.mapper.OrdinaryUserMapper;
import com.word.wordlearning.mapper.WordBookMapper;
import com.word.wordlearning.mapper.WordLearningRecordMapper;
import com.word.wordlearning.mapper.WordMapper;
import com.word.wordlearning.service.AdminstratorService;
import com.word.wordlearning.service.WordBookService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminstratorController {

    private final AdminstratorService adminstratorService;
    private final OrdinaryUserMapper userMapper;
    private final WordBookMapper wordBookMapper;
    private final WordMapper wordMapper;
    private final WordLearningRecordMapper recordMapper;
    private final WordBookService wordBookService;

    public AdminstratorController(AdminstratorService adminstratorService,
                                  OrdinaryUserMapper userMapper,
                                  WordBookMapper wordBookMapper,
                                  WordMapper wordMapper,
                                  WordLearningRecordMapper recordMapper,
                                  WordBookService wordBookService) {
        this.adminstratorService = adminstratorService;
        this.userMapper = userMapper;
        this.wordBookMapper = wordBookMapper;
        this.wordMapper = wordMapper;
        this.recordMapper = recordMapper;
        this.wordBookService = wordBookService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = adminstratorService.login(request);
        if ("登录成功".equals(response.getMessage())) {
            return Result.success(response);
        }
        return Result.error(401, response.getMessage());
    }

    @GetMapping("/stats")
    public Result<AdminStatsDTO> stats() {
        AdminStatsDTO dto = new AdminStatsDTO();
        dto.setUserCount(userMapper.countAll());
        dto.setWordBookCount(wordBookMapper.countAll());
        dto.setWordCount(wordMapper.countAll());
        dto.setTodayRecordCount(recordMapper.countAllToday());
        return Result.success(dto);
    }

    @GetMapping("/users")
    public Result<List<OrdinaryUser>> searchUsers(@RequestParam(defaultValue = "") String keyword) {
        String kw = keyword.isEmpty() ? "%" : keyword;
        List<OrdinaryUser> users = userMapper.searchUsers(kw);
        users.forEach(u -> u.setLoginPassword(null));
        return Result.success(users);
    }

    @PutMapping("/users/{userId}")
    public Result<String> updateUser(@PathVariable String userId, @RequestBody Map<String, String> body) {
        OrdinaryUser user = userMapper.findByUserId(userId);
        if (user == null) return Result.error(404, "用户不存在");

        String status = body.get("accountStatus");
        if (status != null && !status.isEmpty()) {
            userMapper.updateStatus(userId, status);
        }

        String userName = body.get("userName");
        String phone = body.get("phoneNumber");
        String email = body.get("email");
        if (userName != null || phone != null || email != null) {
            if (userName != null) user.setUserName(userName);
            if (phone != null) user.setPhoneNumber(phone);
            if (email != null) user.setEmail(email);
            userMapper.updateProfile(user);
        }

        return Result.success("用户信息已更新");
    }

    @GetMapping("/wordbooks")
    public Result<List<WordBook>> listAllBooks() {
        return Result.success(wordBookService.listAll());
    }

    @GetMapping("/records")
    public Result<List<WordLearningRecord>> viewUserRecords(@RequestParam String userId) {
        return Result.success(recordMapper.findByUserId(userId));
    }
}
