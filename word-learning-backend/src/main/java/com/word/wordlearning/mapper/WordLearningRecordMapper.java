package com.word.wordlearning.mapper;

import com.word.wordlearning.dto.RecentWordDTO;
import com.word.wordlearning.entity.WordLearningRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface WordLearningRecordMapper {

    @Select("SELECT * FROM t_word_learning_record WHERE userId = #{userId}")
    List<WordLearningRecord> findByUserId(String userId);

    @Select("SELECT * FROM t_word_learning_record WHERE recordId = #{recordId}")
    WordLearningRecord findByRecordId(String recordId);

    @Select("SELECT COUNT(DISTINCT wordId) FROM t_word_learning_record WHERE userId = #{userId}")
    int countDistinctWordsByUser(String userId);

    @Select("SELECT COUNT(DISTINCT wordId) FROM t_word_learning_record WHERE userId = #{userId} AND DATE(recordCreateTime) = CURDATE()")
    int countDistinctWordsByUserToday(String userId);

    @Select("SELECT w.englishSpelling AS englishSpelling, w.chineseDefinition AS chineseDefinition " +
            "FROM t_word_learning_record r JOIN t_word w ON r.wordId = w.wordId " +
            "WHERE r.userId = #{userId} GROUP BY r.wordId ORDER BY MAX(r.recordCreateTime) DESC LIMIT #{limit}")
    List<RecentWordDTO> findRecentWordsByUser(@Param("userId") String userId, @Param("limit") int limit);

    @Select("SELECT wb.wordBookName FROM t_word_learning_record r " +
            "JOIN t_word_book wb ON r.learnedWordBookId = wb.wordBookId " +
            "WHERE r.userId = #{userId} GROUP BY r.learnedWordBookId ORDER BY MAX(r.recordCreateTime) DESC LIMIT 1")
    String findCurrentBookName(String userId);

    @Insert("INSERT INTO t_word_learning_record(recordId, userId, learnedWordBookId, wordId, learningDate, recordCreateTime) " +
            "VALUES(#{recordId}, #{userId}, #{learnedWordBookId}, #{wordId}, #{learningDate}, #{recordCreateTime})")
    int insert(WordLearningRecord record);

    @Select("SELECT COUNT(*) FROM t_word_learning_record WHERE DATE(recordCreateTime) = CURDATE()")
    int countAllToday();

    @Select("SELECT MAX(CAST(SUBSTRING(recordId,4) AS UNSIGNED)) FROM t_word_learning_record WHERE recordId LIKE 'REC%'")
    Integer maxNumericId();
}
