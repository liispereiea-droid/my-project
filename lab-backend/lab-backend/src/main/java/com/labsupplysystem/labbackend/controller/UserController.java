package com.labsupplysystem.labbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.labsupplysystem.labbackend.common.api.CommonResult;
import com.labsupplysystem.labbackend.entity.User;
import com.labsupplysystem.labbackend.service.UserService;
import com.labsupplysystem.labbackend.common.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    // 1. 登录
    @PostMapping("/login")
    public CommonResult<Map<String, Object>> login(@RequestBody User user) {
        // 密码加密比对
        String md5Password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8));

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername())
                .eq("password", md5Password);

        User loginUser = userService.getOne(wrapper);

        if (loginUser != null) {
            // 生成 Token
            String token = JwtUtils.generateToken(loginUser.getId(), loginUser.getUsername());
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("data", loginUser); // 把用户信息也放进去
            return CommonResult.success(data, "登录成功");
        } else {
            return CommonResult.failed("账号或密码错误");
        }
    }

    // 2. 注册
    @PostMapping("/register")
    public CommonResult<String> register(@RequestBody User user) {
        // 检查账号是否存在
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", user.getUsername());
        if (userService.count(query) > 0) {
            return CommonResult.failed("账号已存在");
        }

        // 校验导师ID (如果是学生)
        if (user.getTeacherId() != null) {
            User teacher = userService.getById(user.getTeacherId());
            if (teacher == null || !"teacher".equals(teacher.getRole())) {
                return CommonResult.failed("导师ID不存在或无效");
            }
            // 自动设置归属实验室
            user.setLabId(teacher.getLabId());
        }

        // 默认设置
        user.setRole("student");
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8)));

        boolean success = userService.save(user);
        return success ? CommonResult.success("注册成功") : CommonResult.failed("注册失败");
    }

    // 3. 用户列表
    @GetMapping("/list")
    public CommonResult<List<User>> list(@RequestParam(required = false) String name) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.and(w -> w.like("username", name).or().like("nickname", name));
        }
        return CommonResult.success(userService.list(wrapper));
    }

    // 4. 新增用户 (管理员用)
    @PostMapping("/add")
    public CommonResult<User> add(@RequestBody User user) {
        // 判重
        if(userService.count(new QueryWrapper<User>().eq("username", user.getUsername())) > 0) {
            return CommonResult.failed("账号已存在");
        }
        // 默认密码
        if(user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword("123456");
        }
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8)));

        boolean success = userService.save(user);
        return success ? CommonResult.success(user) : CommonResult.failed("添加失败");
    }

    // 5. 修改用户
    @PutMapping("/update")
    public CommonResult<String> update(@RequestBody User user) {
        // 如果改了密码，需要加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8)));
        } else {
            user.setPassword(null); // 不修改密码
        }
        return userService.updateById(user) ? CommonResult.success("修改成功") : CommonResult.failed("修改失败");
    }

    // 6. 删除用户
    @DeleteMapping("/delete/{id}")
    public CommonResult<String> delete(@PathVariable Long id) {
        return userService.removeById(id) ? CommonResult.success("删除成功") : CommonResult.failed("删除失败");
    }
}