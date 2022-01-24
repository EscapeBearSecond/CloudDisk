package com.dyg.service;

import com.dyg.dao.UserDao;
import com.dyg.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserDao userDao;
    public int addUser(User user){
        return userDao.insertUser(user);
    }
    public User login(String username,String password){
        return userDao.queryUser(username,password);
    }
}
