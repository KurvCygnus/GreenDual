package com.wenhua.tcpservice.mapper;

import com.wenhua.tcpservice.pojo.QueryParameter;
import com.wenhua.tcpservice.pojo.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM User WHERE username = #{username} AND password = #{password}")
    User selectUserByUsernameAndPasswordHash(String username, String password);

    @Select("SELECT * FROM User WHERE userId = #{userId}")
    User selectUserById(Integer userId);

    @Select("SELECT * FROM User WHERE username = #{username}")
    User selectUserByUsername(String username);

    List<User> selectUsers(QueryParameter queryParameter);

    int selectUserCount(QueryParameter queryParameter);

    @Update("UPDATE user SET username=#{username}, password=#{password}, userPermission=#{userPermission} WHERE userId=#{userId}")
    void updateUser(User user);

    @Insert("INSERT INTO user(username, password, userPermission, create_time) VALUES(#{username}, #{password}, #{userPermission}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    void addUser(User user);

    @Delete("DELETE FROM user WHERE userId=#{userId}")
    void deleteUser(User user);

    @Update("UPDATE user SET password=#{password} WHERE userId=#{userId}")
    void updatePassword(@Param("userId") Integer userId, @Param("password") String password);

    @Update("UPDATE user SET last_login_time = #{loginTime} WHERE userId = #{userId}")
    void updateLoginTime(@Param("userId") Integer userId, @Param("loginTime") LocalDateTime loginTime);

    @Select("SELECT COUNT(*) FROM user WHERE userPermission = #{permission}")
    int countByPermission(@Param("permission") Integer permission);

    @Select("SELECT COUNT(*) FROM user")
    int countAll();
}
