package com.labsupplysystem.labbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.labsupplysystem.labbackend.entity.User;

public interface UserService extends IService<User> {
    // 定义一个注册方法
    User registerStudent(User user, Long teacherId);
    // 定义一个登录方法
    User login(String username, String password);
}