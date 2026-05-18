package com.word.wordlearning.mapper;

import com.word.wordlearning.entity.Word;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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
}
