package com.word.wordlearning.mapper;

import com.word.wordlearning.entity.WordBook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface WordBookMapper {

    @Select("SELECT * FROM t_word_book WHERE wordBookId = #{wordBookId}")
    WordBook findById(String wordBookId);

    @Select("SELECT * FROM t_word_book")
    List<WordBook> findAll();

    @Select("SELECT COUNT(*) FROM t_word_book")
    int countAll();
}
