package com.word.wordlearning.mapper;

import com.word.wordlearning.entity.Word;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface WordMapper {

    @Select("SELECT * FROM t_word WHERE wordId = #{wordId}")
    Word findById(String wordId);

    @Select("SELECT * FROM t_word WHERE wordBookId = #{wordBookId}")
    List<Word> findByWordBookId(String wordBookId);

    @Select("SELECT * FROM t_word WHERE englishSpelling LIKE CONCAT('%',#{keyword},'%') OR chineseDefinition LIKE CONCAT('%',#{keyword},'%')")
    List<Word> search(@Param("keyword") String keyword);

    @Select("SELECT COUNT(*) FROM t_word")
    int countAll();

    @Insert("INSERT INTO t_word(wordId, wordBookId, englishSpelling, chineseDefinition, exampleSentence, phoneticSymbol, WordPronunciation, wordImage, createTime, updateTime) " +
            "VALUES(#{wordId}, #{wordBookId}, #{englishSpelling}, #{chineseDefinition}, #{exampleSentence}, #{phoneticSymbol}, #{wordPronunciation}, #{wordImage}, NOW(), NOW())")
    void insert(Word word);

    @Update("UPDATE t_word SET englishSpelling=#{englishSpelling}, chineseDefinition=#{chineseDefinition}, " +
            "exampleSentence=#{exampleSentence}, phoneticSymbol=#{phoneticSymbol}, WordPronunciation=#{wordPronunciation}, " +
            "wordImage=#{wordImage}, updateTime=NOW() WHERE wordId=#{wordId}")
    void update(Word word);

    @Delete("DELETE FROM t_word WHERE wordId=#{wordId}")
    void delete(String wordId);
}
