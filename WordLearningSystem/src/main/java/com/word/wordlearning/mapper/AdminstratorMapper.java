package com.word.wordlearning.mapper;

import com.word.wordlearning.entity.Adminstrator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminstratorMapper {

    @Select("SELECT * FROM t_administrator WHERE userId = #{userId}")
    Adminstrator findByUserId(String userId);

    @Select("SELECT * FROM t_administrator WHERE userId = #{userId} AND loginPassword = #{loginPassword}")
    Adminstrator findByUserIdAndPassword(String userId, String loginPassword);
}
