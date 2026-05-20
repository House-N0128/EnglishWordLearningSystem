package com.word.wordlearning.mapper;

import com.word.wordlearning.entity.Word;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface WordMapper {

    @Select("SELECT word_id AS wordId, spelling AS englishSpelling, definition AS chineseDefinition, " +
            "example_sentence AS exampleSentence, phonetic AS phoneticSymbol, " +
            "pronunciation_url AS wordPronunciation, image_url AS wordImage, created_at AS createTime " +
            "FROM t_word WHERE word_id = #{wordId}")
    Word findById(String wordId);

    @Select("SELECT w.word_id AS wordId, w.spelling AS englishSpelling, w.definition AS chineseDefinition, " +
            "w.example_sentence AS exampleSentence, w.phonetic AS phoneticSymbol, " +
            "w.pronunciation_url AS wordPronunciation, w.image_url AS wordImage, w.created_at AS createTime " +
            "FROM t_word w JOIN t_word_book_ref r ON w.word_id = r.word_id WHERE r.word_book_id = #{wordBookId}")
    List<Word> findByWordBookId(String wordBookId);

    @Select("SELECT word_id AS wordId, spelling AS englishSpelling, definition AS chineseDefinition, " +
            "example_sentence AS exampleSentence, phonetic AS phoneticSymbol, " +
            "pronunciation_url AS wordPronunciation, image_url AS wordImage, created_at AS createTime " +
            "FROM t_word WHERE spelling LIKE CONCAT('%',#{keyword},'%') OR definition LIKE CONCAT('%',#{keyword},'%')")
    List<Word> search(@Param("keyword") String keyword);

    @Select("SELECT COUNT(*) FROM t_word")
    int countAll();

    @Insert("INSERT INTO t_word(word_id, spelling, definition, example_sentence, phonetic, pronunciation_url, image_url, created_at) " +
            "VALUES(#{wordId}, #{englishSpelling}, #{chineseDefinition}, #{exampleSentence}, #{phoneticSymbol}, #{wordPronunciation}, #{wordImage}, NOW())")
    void insert(Word word);

    @Insert("INSERT IGNORE INTO t_word_book_ref(word_book_id, word_id) VALUES(#{wordBookId}, #{wordId})")
    void insertBookRef(@Param("wordBookId") String wordBookId, @Param("wordId") String wordId);

    @Update("UPDATE t_word SET spelling=#{englishSpelling}, definition=#{chineseDefinition}, " +
            "example_sentence=#{exampleSentence}, phonetic=#{phoneticSymbol}, " +
            "pronunciation_url=#{wordPronunciation}, image_url=#{wordImage} WHERE word_id=#{wordId}")
    void update(Word word);

    @Delete("DELETE FROM t_word_book_ref WHERE word_book_id=#{wordBookId} AND word_id=#{wordId}")
    int deleteBookRef(@Param("wordBookId") String wordBookId, @Param("wordId") String wordId);

    @Select("SELECT COUNT(*) FROM t_word_book_ref WHERE word_id=#{wordId}")
    int countBookRefs(String wordId);

    @Delete("DELETE FROM t_word WHERE word_id=#{wordId}")
    void delete(String wordId);
}
