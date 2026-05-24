package com.word.wordlearning.mapper;

import com.word.wordlearning.entity.Adminstrator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminstratorMapper {

    @Select("SELECT * FROM t_administrator WHERE userId = #{userId}")
    Adminstrator findByUserId(String userId);

    @Select("SELECT * FROM t_administrator WHERE userId = #{userId} AND loginPassword = #{loginPassword}")
    Adminstrator findByUserIdAndPassword(String userId, String loginPassword);

    @Update("UPDATE t_administrator SET userId = #{newUserId} WHERE userId = #{oldUserId}")
    int updateUserId(String oldUserId, String newUserId);

    @Update("UPDATE t_administrator SET loginPassword = #{newPassword} WHERE userId = #{userId}")
    int updatePassword(String userId, String newPassword);

    @Update("UPDATE t_administrator SET phoneNumber = #{phoneNumber}, email = #{email} WHERE userId = #{userId}")
    int updateProfile(Adminstrator admin);

    @Select("SELECT * FROM t_administrator WHERE email = #{email} AND userId != #{excludeUserId}")
    Adminstrator findByEmailExcludeSelf(String email, String excludeUserId);
}