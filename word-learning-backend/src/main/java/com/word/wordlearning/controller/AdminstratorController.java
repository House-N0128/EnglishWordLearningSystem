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
        dto.setTodayActiveUserCount(recordMapper.countTodayActiveUsers());
        
        // 计算今日人均学习单词量
        int activeUsers = dto.getTodayActiveUserCount();
        int todayRecords = dto.getTodayRecordCount();
        double avgWords = activeUsers > 0 ? (double) todayRecords / activeUsers : 0;
        dto.setAvgWordsPerUserToday(Math.round(avgWords * 100.0) / 100.0);
        
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
    public Result<String> updateUserInfo(@PathVariable String userId, @RequestBody Map<String, String> body) {
        // 更新状态
        if (body.containsKey("accountStatus")) {
            String status = body.get("accountStatus");
            userMapper.updateStatus(userId, status);
            return Result.success("用户状态已更新");
        }
        // 更新其他信息（userName, phoneNumber, email）
        OrdinaryUser user = new OrdinaryUser();
        user.setUserId(userId);
        user.setUserName(body.get("userName"));
        user.setPhoneNumber(body.get("phoneNumber"));
        user.setEmail(body.get("email"));
        userMapper.updateProfile(user);
        return Result.success("用户信息已更新");
    }

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
    public Result<List<WordLearningRecord>> viewUserRecords(@RequestParam(required = false) String userId) {
        System.out.println("========== 学习记录查询开始 ==========");
        System.out.println("请求参数 userId: " + userId);
        System.out.println("userId是否为null: " + (userId == null));
        System.out.println("userId是否为空字符串: " + (userId != null && userId.isEmpty()));
        System.out.println("userId是否等于'all': " + "all".equals(userId));
        
        List<WordLearningRecord> records;
        if (userId == null || userId.isEmpty() || "all".equals(userId)) {
            System.out.println("执行查询：findAll()");
            records = recordMapper.findAll();
        } else {
            System.out.println("执行查询：findByUserId(" + userId + ")");
            records = recordMapper.findByUserId(userId);
        }
        
        System.out.println("查询结果数量: " + records.size());
        if (records.size() > 0) {
            System.out.println("第一条记录: " + records.get(0));
        }
        System.out.println("========== 学习记录查询结束 ==========");
        
        return Result.success(records);
    }

    @PutMapping("/updateName")
    public Result<String> updateAdminName(@RequestBody Map<String, String> body) {
        String newAdminName = body.get("adminName");
        if (newAdminName == null || newAdminName.trim().isEmpty()) {
            return Result.error(400, "管理员名称不能为空");
        }
        
        // 从请求中获取当前管理员ID（实际项目中应从登录状态获取）
        // 这里假设当前管理员是默认的 "admin"
        String currentAdminId = "admin";
        
        boolean success = adminstratorService.updateAdminName(currentAdminId, newAdminName.trim());
        if (success) {
            return Result.success("管理员名称修改成功");
        } else {
            return Result.error(500, "修改失败，该用户名已存在");
        }
    }

    @GetMapping("/info")
    public Result<com.word.wordlearning.entity.Adminstrator> getAdminInfo() {
        // 这里假设当前管理员是默认的 "admin"
        String currentAdminId = "admin";
        com.word.wordlearning.entity.Adminstrator admin = adminstratorService.getAdminInfo(currentAdminId);
        if (admin != null) {
            return Result.success(admin);
        } else {
            return Result.error(404, "管理员不存在");
        }
    }

    @PostMapping("/changePassword")
    public Result<String> changePassword(@RequestBody Map<String, String> body) {
        // 打印接收到的参数，便于排查问题
        System.out.println("========== 修改密码请求 ==========");
        System.out.println("接收到的参数: " + body);
        
        String userId = body.get("userId");
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        String confirmPassword = body.get("confirmPassword");
        
        // 参数校验
        if (userId == null || userId.isEmpty()) {
            return Result.error(400, "管理员ID不能为空");
        }
        if (oldPassword == null || oldPassword.isEmpty()) {
            return Result.error(400, "原密码不能为空");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Result.error(400, "新密码长度至少6位");
        }
        if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
            return Result.error(400, "两次输入的密码不一致");
        }
        
        // 使用前端传入的管理员ID
        System.out.println("当前管理员ID: " + userId);
        
        boolean success = adminstratorService.changePassword(userId, oldPassword, newPassword);
        
        if (success) {
            System.out.println("密码修改成功");
            return Result.success("密码修改成功");
        } else {
            System.out.println("密码修改失败：原密码错误");
            return Result.error(400, "原密码错误");
        }
    }
}