package com.word.wordlearning.controller;

import com.word.wordlearning.dto.AdminStatsDTO;
import com.word.wordlearning.dto.LoginRequest;
import com.word.wordlearning.dto.LoginResponse;
import com.word.wordlearning.dto.Result;
import com.word.wordlearning.mapper.OrdinaryUserMapper;
import com.word.wordlearning.mapper.WordBookMapper;
import com.word.wordlearning.mapper.WordLearningRecordMapper;
import com.word.wordlearning.mapper.WordMapper;
import com.word.wordlearning.service.AdminstratorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminstratorController {

    private final AdminstratorService adminstratorService;
    private final OrdinaryUserMapper userMapper;
    private final WordBookMapper wordBookMapper;
    private final WordMapper wordMapper;
    private final WordLearningRecordMapper recordMapper;

    public AdminstratorController(AdminstratorService adminstratorService,
                                  OrdinaryUserMapper userMapper,
                                  WordBookMapper wordBookMapper,
                                  WordMapper wordMapper,
                                  WordLearningRecordMapper recordMapper) {
        this.adminstratorService = adminstratorService;
        this.userMapper = userMapper;
        this.wordBookMapper = wordBookMapper;
        this.wordMapper = wordMapper;
        this.recordMapper = recordMapper;
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
}
