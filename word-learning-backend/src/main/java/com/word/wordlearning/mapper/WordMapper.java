package com.word.wordlearning.mapper;

import com.word.wordlearning.entity.Word;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface WordMapper {

    @Select("SELECT word_id AS wordId, spelling AS englishSpelling, definition AS chineseDefinition, part_of_speech AS partOfSpeech, " +
            "example_sentence AS exampleSentence, phonetic AS phoneticSymbol, " +
            "pronunciation_url AS wordPronunciation, image_url AS wordImage, created_at AS createTime " +
            "FROM t_word WHERE word_id = #{wordId}")
    Word findById(String wordId);

    @Select("SELECT w.word_id AS wordId, w.spelling AS englishSpelling, w.definition AS chineseDefinition, " +
            "w.part_of_speech AS partOfSpeech, " +
            "w.example_sentence AS exampleSentence, w.phonetic AS phoneticSymbol, " +
            "w.pronunciation_url AS wordPronunciation, w.image_url AS wordImage, w.created_at AS createTime " +
            "FROM t_word w JOIN t_word_book_ref r ON w.word_id = r.word_id WHERE r.word_book_id = #{wordBookId}")
    List<Word> findByWordBookId(String wordBookId);

    @Select("SELECT word_id AS wordId, spelling AS englishSpelling, definition AS chineseDefinition, part_of_speech AS partOfSpeech, " +
            "example_sentence AS exampleSentence, phonetic AS phoneticSymbol, " +
            "pronunciation_url AS wordPronunciation, image_url AS wordImage, created_at AS createTime " +
            "FROM t_word WHERE spelling LIKE CONCAT('%',#{keyword},'%') OR definition LIKE CONCAT('%',#{keyword},'%')")
    List<Word> search(@Param("keyword") String keyword);

    @Select("SELECT * FROM t_word WHERE spelling = #{spelling}")
    Word findBySpelling(@Param("spelling") String spelling);

    @Select("SELECT COUNT(*) FROM t_word")
    int countAll();

    @Select("SELECT MAX(CAST(SUBSTRING(word_id, 3) AS UNSIGNED)) FROM t_word WHERE word_id LIKE 'WD%'")
    Integer maxWordIdNum();

    @Insert("INSERT INTO t_word(word_id, spelling, definition, part_of_speech, example_sentence, phonetic, pronunciation_url, image_url, created_at) " +
            "VALUES(#{wordId}, #{englishSpelling}, #{chineseDefinition}, #{partOfSpeech}, #{exampleSentence}, #{phoneticSymbol}, #{wordPronunciation}, #{wordImage}, NOW())")
    void insert(Word word);

    @Insert("INSERT IGNORE INTO t_word_book_ref(word_book_id, word_id) VALUES(#{wordBookId}, #{wordId})")
    void insertBookRef(@Param("wordBookId") String wordBookId, @Param("wordId") String wordId);

    @Select("SELECT COUNT(*) FROM t_word_book_ref WHERE word_book_id = #{bookId} AND word_id = #{wordId}")
    int existsBookRef(@Param("bookId") String bookId, @Param("wordId") String wordId);

    @Update("UPDATE t_word SET spelling=#{englishSpelling}, definition=#{chineseDefinition}, part_of_speech=#{partOfSpeech}, " +
            "example_sentence=#{exampleSentence}, phonetic=#{phoneticSymbol}, " +
            "pronunciation_url=#{wordPronunciation}, image_url=#{wordImage} WHERE word_id=#{wordId}")
    void update(Word word);

    @Delete("DELETE FROM t_word_book_ref WHERE word_book_id=#{wordBookId} AND word_id=#{wordId}")
    int deleteBookRef(@Param("wordBookId") String wordBookId, @Param("wordId") String wordId);

    @Select("SELECT COUNT(*) FROM t_word_book_ref WHERE word_id=#{wordId}")
    int countBookRefs(String wordId);

    @Delete("DELETE FROM t_word WHERE word_id=#{wordId}")
    void delete(String wordId);

    @Delete("DELETE FROM t_word_book_ref WHERE word_book_id = #{bookId}")
    int deleteAllBookRefsByBookId(@Param("bookId") String bookId);

    @Select("SELECT word_id FROM t_word_book_ref WHERE word_book_id = #{bookId}")
    List<String> findWordIdsByBookId(@Param("bookId") String bookId);

    @Select("SELECT w.word_id AS wordId, w.spelling AS englishSpelling, w.definition AS chineseDefinition, w.part_of_speech AS partOfSpeech, " +
            "w.example_sentence AS exampleSentence, w.phonetic AS phoneticSymbol, " +
            "w.pronunciation_url AS wordPronunciation, w.image_url AS wordImage, w.created_at AS createTime, " +
            "(SELECT r.word_book_id FROM t_word_book_ref r WHERE r.word_id = w.word_id LIMIT 1) AS wordBookId " +
            "FROM t_word w")
    List<Word> findAllWords();
}