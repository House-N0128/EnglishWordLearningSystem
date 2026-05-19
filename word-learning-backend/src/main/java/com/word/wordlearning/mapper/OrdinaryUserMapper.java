package com.word.wordlearning.mapper;

import com.word.wordlearning.entity.OrdinaryUser;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface OrdinaryUserMapper {

    @Select("SELECT * FROM t_ordinary_user WHERE userId = #{userId}")
    OrdinaryUser findByUserId(String userId);

    @Select("SELECT * FROM t_ordinary_user WHERE userId = #{userId} AND loginPassword = #{loginPassword}")
    OrdinaryUser findByUserIdAndPassword(String userId, String loginPassword);

    @Insert("INSERT INTO t_ordinary_user (userId, loginPassword, accountStatus, registerTime, lastLoginTime, userName, phoneNumber, email) " +
            "VALUES (#{userId}, #{loginPassword}, '正常', NOW(), NOW(), #{userName}, #{phoneNumber}, #{email})")
    void insert(OrdinaryUser user);

    @Update("UPDATE t_ordinary_user SET userName = #{userName}, phoneNumber = #{phoneNumber}, email = #{email} WHERE userId = #{userId}")
    void updateProfile(OrdinaryUser user);

    @Update("UPDATE t_ordinary_user SET loginPassword = #{newPassword} WHERE userId = #{userId}")
    void updatePassword(@Param("userId") String userId, @Param("newPassword") String newPassword);

    @Delete("DELETE FROM t_ordinary_user WHERE userId = #{userId}")
    void deleteByUserId(String userId);

    @Select("SELECT COUNT(*) FROM t_ordinary_user")
    int countAll();

    @Select("SELECT * FROM t_ordinary_user WHERE userId LIKE CONCAT('%',#{keyword},'%') OR userName LIKE CONCAT('%',#{keyword},'%')")
    List<OrdinaryUser> searchUsers(@Param("keyword") String keyword);

    @Update("UPDATE t_ordinary_user SET accountStatus=#{status} WHERE userId=#{userId}")
    void updateStatus(@Param("userId") String userId, @Param("status") String status);
}
