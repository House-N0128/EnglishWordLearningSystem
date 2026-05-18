package com.word.wordlearning.controller;

import com.word.wordlearning.dto.HomeStatsDTO;
import com.word.wordlearning.dto.RecentWordDTO;
import com.word.wordlearning.dto.Result;
import com.word.wordlearning.entity.WordLearningRecord;
import com.word.wordlearning.mapper.WordLearningRecordMapper;
import com.word.wordlearning.service.LearningRecordService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/records")
public class LearningRecordController {

    private final LearningRecordService recordService;
    private final WordLearningRecordMapper recordMapper;

    public LearningRecordController(LearningRecordService recordService,
                                     WordLearningRecordMapper recordMapper) {
        this.recordService = recordService;
        this.recordMapper = recordMapper;
    }

    @GetMapping("/stats")
    public Result<HomeStatsDTO> stats(@RequestHeader("X-User-Id") String userId) {
        return Result.success(recordService.getHomeStats(userId));
    }

    @GetMapping("/recent")
    public Result<List<RecentWordDTO>> recent(@RequestHeader("X-User-Id") String userId) {
        return Result.success(recordService.getRecentWords(userId, 3));
    }

    @GetMapping("/list")
    public Result<List<WordLearningRecord>> list(@RequestHeader("X-User-Id") String userId) {
        return Result.success(recordMapper.findByUserId(userId));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestHeader("X-User-Id") String userId,
                              @RequestBody Map<String, String> body) {
        WordLearningRecord record = new WordLearningRecord();
        record.setRecordId(UUID.randomUUID().toString().substring(0, 20));
        record.setUserId(userId);
        record.setLearnedWordBookId(body.get("learnedWordBookId"));
        record.setWordId(body.get("wordId"));
        record.setLearningDate(LocalDate.now());
        record.setRecordCreateTime(LocalDateTime.now());
        recordMapper.insert(record);
        return Result.success("学习记录已保存");
    }
}
