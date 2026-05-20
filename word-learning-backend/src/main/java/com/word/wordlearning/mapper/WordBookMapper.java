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

    @Select("SELECT COUNT(*) FROM t_word_book")
    int countAll();

    @Insert("INSERT INTO t_word_book(wordBookId, wordBookName, difficultyLevel, wordBookDescription, wordCount, createTime, updateTime, wordBookStatus) " +
            "VALUES(#{wordBookId}, #{wordBookName}, #{difficultyLevel}, #{wordBookDescription}, 0, NOW(), NOW(), '已上线')")
    void insert(WordBook book);

    @Update("UPDATE t_word_book SET wordBookName=#{wordBookName}, difficultyLevel=#{difficultyLevel}, wordBookDescription=#{wordBookDescription}, updateTime=NOW() WHERE wordBookId=#{wordBookId}")
    void update(WordBook book);

    @Update("UPDATE t_word_book SET wordBookStatus='已下线', updateTime=NOW() WHERE wordBookId=#{wordBookId}")
    void delete(String wordBookId);

    @Select("SELECT wordBookId FROM t_word_book WHERE wordBookId LIKE 'WB%' ORDER BY wordBookId DESC LIMIT 1")
    String maxBookId();

    @Select("SELECT * FROM t_word_book WHERE wordBookName = #{name}")
    WordBook findByName(String name);
}
