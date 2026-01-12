package com.labsupplysystem.labbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.labsupplysystem.labbackend.entity.User;
import com.labsupplysystem.labbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    // 1. 登录接口 (保持不变)
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.login(username, password);
            result.put("code", 200);
            result.put("msg", "登录成功");
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

        // 检查用户名是否重复
        long count = userService.count(new QueryWrapper<User>().eq("username", user.getUsername()));
        if (count > 0) {
            result.put("code", 400);
            result.put("msg", "用户名已存在");
            return result;
        }
        // 默认密码
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword("123456");
        }
        // 保存用户 (MyBatis Plus 会自动把生成的 ID 回填到 user 对象里)
        boolean success = userService.save(user);
        if (success) {
            result.put("code", 200);
            result.put("msg", "添加成功");
            result.put("data", user); // <--- 关键！把带 ID 的用户对象传回去
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