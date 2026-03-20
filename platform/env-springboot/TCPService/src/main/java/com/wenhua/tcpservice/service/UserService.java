package com.wenhua.tcpservice.service;


import com.wenhua.tcpservice.pojo.QueryParameter;
import com.wenhua.tcpservice.pojo.User;
import org.apache.ibatis.annotations.Update;

import java.util.List;

//用户相关
public interface UserService {

    //查询用户接口
    //参数：用户名，密码
    //返回值：用户
    public User selectUser(String username, String password);

    //条件查询用户
    public List<User> selectUsers(QueryParameter queryParameter);

    //修改用户
    public void updateUser(User user);

    //添加用户
    public void addUser(User user);

    //删除用户
    public void deleteUser(User user);



}
