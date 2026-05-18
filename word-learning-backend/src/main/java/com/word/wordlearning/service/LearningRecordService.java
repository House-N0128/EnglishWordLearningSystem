package com.word.wordlearning.service;

import com.word.wordlearning.dto.HomeStatsDTO;
import com.word.wordlearning.dto.RecentWordDTO;
import com.word.wordlearning.mapper.WordLearningRecordMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LearningRecordService {

    private final WordLearningRecordMapper recordMapper;

    public LearningRecordService(WordLearningRecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    public HomeStatsDTO getHomeStats(String userId) {
        int today = recordMapper.countDistinctWordsByUserToday(userId);
        int total = recordMapper.countDistinctWordsByUser(userId);
        String bookName = recordMapper.findCurrentBookName(userId);
        if (bookName == null) {
            bookName = "暂无";
        }
        return new HomeStatsDTO(today, total, bookName);
    }

    public List<RecentWordDTO> getRecentWords(String userId, int limit) {
        return recordMapper.findRecentWordsByUser(userId, limit);
    }
}
