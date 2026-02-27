package com.labsupplysystem.labbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.labsupplysystem.labbackend.entity.User;
import com.labsupplysystem.labbackend.mapper.UserMapper;
import com.labsupplysystem.labbackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public User registerStudent(User user, Long teacherId) {
        // 1. 校验导师是否存在
        User teacher = this.getById(teacherId);
        if (teacher == null || !"teacher".equals(teacher.getRole())) {
            throw new RuntimeException("注册失败：导师ID不存在或该用户不是导师！");
        }

        // 2. 校验用户名是否重复
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", user.getUsername());
        if (this.count(query) > 0) {
            throw new RuntimeException("注册失败：用户名已存在！");
        }

        // 3. 补全信息并保存
        user.setRole("student");
        user.setTeacherId(teacherId);
        // 默认分配到导师所在的实验室
        user.setLabId(teacher.getLabId()); 
        
        this.save(user);
        return user;
    }

    @Override
    public User login(String username, String password) {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", username);
        query.eq("password", password); // 实际项目中密码要加密比对，这里先明文
        User user = this.getOne(query);
        if (user == null) {
            throw new RuntimeException("登录失败：用户名或密码错误！");
        }
        return user;
    }
}