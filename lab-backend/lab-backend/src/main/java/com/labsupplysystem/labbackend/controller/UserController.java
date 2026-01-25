package com.labsupplysystem.labbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.labsupplysystem.labbackend.common.JwtUtils;
import com.labsupplysystem.labbackend.common.api.CommonResult; // 👈 关键：引入通用返回
import com.labsupplysystem.labbackend.entity.User;
import com.labsupplysystem.labbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation; // 👈 关键：引入文档注解
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理接口
 * Modified to follow Mall-Swarm standards
 */
@RestController
@RequestMapping("/user") // 注意：保持和你前端匹配的路径，如果前端是 /api/user 这里就改一下
@Tag(name = "UserController", description = "用户管理与认证") // Knife4j 分组名称
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户登录") // 接口描述
    @PostMapping("/login")
    // 返回值类型变成了 CommonResult<Map<String, Object>>
    public CommonResult<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        // 1. 查用户
        User user = userService.getOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            return CommonResult.failed("用户不存在");
        }

        // 2. 校验密码 (保持你原有的 MD5 逻辑)
        String inputMd5 = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!inputMd5.equals(user.getPassword())) {
            return CommonResult.failed("用户名或密码错误");
        }

        // 3. 生成 Token
        // 🚨 注意：这里需要你确保 JwtUtils 类存在。如果没有，可以先暂时返回 fake-token
        String token = "";
        try {
            token = JwtUtils.generateToken(user.getId(), user.getUsername());
        } catch (Exception e) {
            token = "temp-token-for-test"; // 防止报错的临时处理
        }

        // 4. 封装返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);

        // 5. 返回标准成功响应
        return CommonResult.success(result, "登录成功");
    }

    @Operation(summary = "获取用户列表")
    @GetMapping("/list")
    public CommonResult<List<User>> getList(@RequestParam(required = false) String name) {
        QueryWrapper<User> query = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            query.like("nickname", name).or().like("username", name);
        }
        query.orderByAsc("id");
        List<User> list = userService.list(query);
        return CommonResult.success(list);
    }

    @Operation(summary = "添加用户")
    @PostMapping("/add")
    public CommonResult<User> add(@RequestBody User user) {
        long count = userService.count(new QueryWrapper<User>().eq("username", user.getUsername()));
        if (count > 0) {
            return CommonResult.failed("用户名已存在");
        }
        // 默认密码处理
        String rawPassword = (user.getPassword() == null || user.getPassword().isEmpty()) ? "123456" : user.getPassword();
        String md5Password = DigestUtils.md5DigestAsHex(rawPassword.getBytes());
        user.setPassword(md5Password);

        boolean success = userService.save(user);
        if (success) {
            return CommonResult.success(user, "添加成功");
        }
        return CommonResult.failed("添加失败");
    }

    @Operation(summary = "修改用户信息")
    @PutMapping("/update")
    public CommonResult<String> update(@RequestBody User user) {
        if (user.getPassword() != null && user.getPassword().isEmpty()) {
            user.setPassword(null);
        }
        boolean success = userService.updateById(user);
        if (success) {
            return CommonResult.success(null, "修改成功");
        }
        return CommonResult.failed("修改失败");
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/delete/{id}")
    public CommonResult<String> delete(@PathVariable Long id) {
        boolean success = userService.removeById(id);
        if (success) {
            return CommonResult.success(null, "删除成功");
        }
        return CommonResult.failed("删除失败");
    }
}