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
            "w.englishSpelling, w.chineseDefinition, w.phoneticSymbol " +
            "FROM t_collection c JOIN t_word w ON c.wordId = w.wordId " +
            "WHERE c.userId = #{userId} ORDER BY c.collectionTime DESC")
    List<Collection> findByUserIdWithWord(String userId);

    @Insert("INSERT INTO t_collection (collectionId, userId, wordId, collectionTime) " +
            "VALUES (#{collectionId}, #{userId}, #{wordId}, #{collectionTime})")
    void insert(Collection collection);

    @Select("SELECT * FROM t_collection WHERE userId = #{userId} AND wordId = #{wordId}")
    Collection findByUserIdAndWordId(@Param("userId") String userId, @Param("wordId") String wordId);

    @Delete("DELETE FROM t_collection WHERE userId = #{userId} AND wordId = #{wordId}")
    int deleteByUserIdAndWordId(@Param("userId") String userId, @Param("wordId") String wordId);
}
