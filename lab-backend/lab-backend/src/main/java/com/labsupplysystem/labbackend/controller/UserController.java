package com.labsupplysystem.labbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.labsupplysystem.labbackend.entity.User;
import com.labsupplysystem.labbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.labsupplysystem.labbackend.common.JwtUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    // 密码加密器
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password"); // 用户输入的明文 "123456"
        Map<String, Object> result = new HashMap<>();

        try {
            // 查用户
            User user = userService.getOne(new QueryWrapper<User>().eq("username", username));
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // === 核心修改：将输入密码转为 MD5 后再比对 ===
            // "123456" -> "e10adc3949ba59abbe56e057f20f883e"
            String inputMd5 = DigestUtils.md5DigestAsHex(password.getBytes());

            // 直接字符串比对
            if (!inputMd5.equals(user.getPassword())) {
                throw new RuntimeException("用户名或密码错误");
            }

            // 生成 Token (保持不变)
            String token = JwtUtils.generateToken(user.getId(), user.getUsername());

            result.put("code", 200);
            result.put("msg", "登录成功");
            result.put("token", token);
            result.put("data", user);

        } catch (Exception e) {
            result.put("code", 400);
            result.put("msg", e.getMessage());
        }
        return result;
    }


    // === 新增：用户列表 (支持按名字搜索) ===
    @GetMapping("/list")
    public List<User> getList(@RequestParam(required = false) String name) {
        QueryWrapper<User> query = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            query.like("nickname", name).or().like("username", name);
        }
        // 按ID排序
        query.orderByAsc("id");
        return userService.list(query);
    }

    // === 新增：添加用户 ===
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();

        // 检查重名 (省略...)
        long count = userService.count(new QueryWrapper<User>().eq("username", user.getUsername()));
        if (count > 0) {
            result.put("code", 400);
            result.put("msg", "用户名已存在");
            return result;
        }

        // 默认密码处理
        String rawPassword = (user.getPassword() == null || user.getPassword().isEmpty()) ? "123456" : user.getPassword();

        // === 核心修改：存入数据库前转为 MD5 ===
        String md5Password = DigestUtils.md5DigestAsHex(rawPassword.getBytes());
        user.setPassword(md5Password);

        boolean success = userService.save(user);
        if (success) {
            result.put("code", 200);
            result.put("msg", "添加成功");
            result.put("data", user);
        } else {
            result.put("code", 500);
            result.put("msg", "添加失败");
        }
        return result;
    }

    // === 新增：修改用户 ===
    @PutMapping("/update")
    public String update(@RequestBody User user) {
        // 如果密码为空，说明不修改密码，设为null防止MyBatis更新它
        if (user.getPassword() != null && user.getPassword().isEmpty()) {
            user.setPassword(null);
        }
        userService.updateById(user);
        return "success";
    }

    // === 新增：删除用户 ===
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        userService.removeById(id);
        return "success";
    }
}