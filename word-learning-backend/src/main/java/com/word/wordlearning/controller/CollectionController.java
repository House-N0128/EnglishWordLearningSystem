package com.word.wordlearning.controller;

import com.word.wordlearning.dto.Result;
import com.word.wordlearning.entity.Collection;
import com.word.wordlearning.mapper.CollectionMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {

    private final CollectionMapper collectionMapper;

    public CollectionController(CollectionMapper collectionMapper) {
        this.collectionMapper = collectionMapper;
    }

    @GetMapping
    public Result<List<Collection>> list(@RequestHeader("X-User-Id") String userId) {
        return Result.success(collectionMapper.findByUserIdWithWord(userId));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestHeader("X-User-Id") String userId,
                              @RequestBody Map<String, String> body) {
        String wordId = body.get("wordId");
        Collection exist = collectionMapper.findByUserIdAndWordId(userId, wordId);
        if (exist != null) {
            return Result.error(400, "该单词已收藏");
        }
        Collection c = new Collection();
        c.setCollectionId(UUID.randomUUID().toString().substring(0, 20));
        c.setUserId(userId);
        c.setWordId(wordId);
        c.setCollectionTime(LocalDateTime.now());
        collectionMapper.insert(c);
        return Result.success("收藏成功");
    }

    @DeleteMapping("/remove")
    public Result<String> remove(@RequestHeader("X-User-Id") String userId,
                                  @RequestBody Map<String, String> body) {
        String wordId = body.get("wordId");
        int n = collectionMapper.deleteByUserIdAndWordId(userId, wordId);
        if (n > 0) {
            return Result.success("已取消收藏");
        }
        return Result.error(404, "未找到该收藏");
    }
}
