package com.wenhua.tcpservice.service.impl;

import com.wenhua.tcpservice.mapper.UserMapper;
import com.wenhua.tcpservice.pojo.QueryParameter;
import com.wenhua.tcpservice.pojo.User;
import com.wenhua.tcpservice.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private UserMapper userMapper;

    @Override
    public User selectUser(String username, String password) {

        //调用mapper层方法
        /* 实际上这里要处理麻麻加密等东西,现在测试省略了 */
        
        return userMapper.selectUserByUsernameAndPasswordHash(username, password);
    }

    @Override
    public List<User> selectUsers(QueryParameter queryParameter) {
        //查询一群用户
        queryParameter.calculateOffset();

        return userMapper.selectUsers(queryParameter);
    }

    //修改用户
    @Override
    public void updateUser(User user) {
        //调用mapper
        userMapper.updateUser(user);
    }

    //添加用户
    @Override
    public void addUser(User user) {
        userMapper.addUser(user);
    }

    @Override
    public void deleteUser(User user) {
        userMapper.deleteUser(user);
    }
}
