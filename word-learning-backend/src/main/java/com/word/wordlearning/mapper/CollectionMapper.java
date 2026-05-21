package com.word.wordlearning.mapper;

import com.word.wordlearning.entity.Collection;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CollectionMapper {

    @Select("SELECT * FROM t_collection WHERE userId = #{userId}")
    List<Collection> findByUserId(String userId);

    @Select("SELECT * FROM t_collection WHERE collectionId = #{collectionId}")
    Collection findById(String collectionId);

    @Select("SELECT c.collectionId, c.userId, c.wordId, c.collectionTime, " +
            "w.spelling AS englishSpelling, w.definition AS chineseDefinition, w.phonetic AS phoneticSymbol " +
            "FROM t_collection c JOIN t_word w ON c.wordId = w.word_id " +
            "WHERE c.userId = #{userId} ORDER BY c.collectionTime DESC")
    List<Collection> findByUserIdWithWord(String userId);

    @Insert("INSERT INTO t_collection (collectionId, userId, wordId, collectionTime) " +
            "VALUES (#{collectionId}, #{userId}, #{wordId}, #{collectionTime})")
    void insert(Collection collection);

    @Select("SELECT * FROM t_collection WHERE userId = #{userId} AND wordId = #{wordId}")
    Collection findByUserIdAndWordId(@Param("userId") String userId, @Param("wordId") String wordId);

    @Delete("DELETE FROM t_collection WHERE userId = #{userId} AND wordId = #{wordId}")
    int deleteByUserIdAndWordId(@Param("userId") String userId, @Param("wordId") String wordId);

    @Select("SELECT MAX(CAST(collectionId AS UNSIGNED)) FROM t_collection WHERE collectionId REGEXP '^[0-9]+$'")
    Integer maxNumericId();

    @Delete("DELETE FROM t_collection WHERE wordId = #{wordId}")
    void deleteByWordId(@Param("wordId") String wordId);
}
