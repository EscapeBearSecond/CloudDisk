package com.dyg.dao;

import com.dyg.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDao {
    int insertUser(User user);
    User queryUser(@Param("username") String username, @Param("password") String password);
}
