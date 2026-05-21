package com.word.wordlearning.mapper;

import com.word.wordlearning.entity.WordBook;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface WordBookMapper {

    @Select("SELECT * FROM t_word_book WHERE wordBookId = #{wordBookId}")
    WordBook findById(String wordBookId);

    @Select("SELECT * FROM t_word_book")
    List<WordBook> findAll();

    @Select("SELECT * FROM t_word_book WHERE wordBookStatus = '已上架'")
    List<WordBook> findAllOnline();

    @Select("SELECT COUNT(*) FROM t_word_book")
    int countAll();

    @Insert("INSERT INTO t_word_book(wordBookId, wordBookName, difficultyLevel, wordBookDescription, wordCount, createTime, updateTime, wordBookStatus) " +
            "VALUES(#{wordBookId}, #{wordBookName}, #{difficultyLevel}, #{wordBookDescription}, 0, NOW(), NOW(), #{wordBookStatus})")
    void insert(WordBook book);

    @Update("UPDATE t_word_book SET wordBookName=#{wordBookName}, difficultyLevel=#{difficultyLevel}, wordBookDescription=#{wordBookDescription}, wordBookStatus=#{wordBookStatus}, updateTime=NOW() WHERE wordBookId=#{wordBookId}")
    void update(WordBook book);

    @Update("UPDATE t_word_book SET wordBookStatus='未上架', updateTime=NOW() WHERE wordBookId=#{wordBookId}")
    void delete(String wordBookId);

    @Select("SELECT wordBookId FROM t_word_book WHERE wordBookId LIKE 'WB%' ORDER BY wordBookId DESC LIMIT 1")
    String maxBookId();

    @Select("SELECT * FROM t_word_book WHERE wordBookName = #{name}")
    WordBook findByName(String name);

    @Select("SELECT COUNT(*) FROM t_word_book_ref WHERE word_book_id = #{wordBookId}")
    int countWordsInBook(String wordBookId);

    @Update("UPDATE t_word_book SET wordCount = (SELECT COUNT(*) FROM t_word_book_ref WHERE word_book_id = #{wordBookId}) WHERE wordBookId = #{wordBookId}")
    void syncWordCount(String wordBookId);
}
