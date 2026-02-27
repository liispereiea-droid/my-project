package com.labsupplysystem.labbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.labsupplysystem.labbackend.common.api.CommonResult; // 记得导包
import com.labsupplysystem.labbackend.entity.Apply;
import com.labsupplysystem.labbackend.entity.User;
import com.labsupplysystem.labbackend.service.ApplyService;
import com.labsupplysystem.labbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.labsupplysystem.labbackend.common.UserContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/apply")
@CrossOrigin
public class ApplyController {

    @Autowired
    private ApplyService applyService;
    @Autowired
    private UserService userService;

    // 1. 提交申请
    @PostMapping("/submit")
    public CommonResult<String> submit(@RequestBody Apply apply) {
        try {
            User user = UserContext.getUser();
            if(user == null) return CommonResult.failed("请先登录");

            apply.setUserId(user.getId());
            apply.setUserName(user.getNickname());
            apply.setLabId(user.getLabId());

            if ("teacher".equals(user.getRole())) {
                apply.setStatus(1); // 导师自申，直接过
                apply.setTeacherAuditOpinion("系统自动通过(导师自申)");
                apply.setTeacherAuditTime(LocalDateTime.now());
            } else {
                apply.setStatus(0); // 学生申请
            }
            apply.setCreateTime(LocalDateTime.now());
            applyService.save(apply);
            return CommonResult.success("提交成功");
        } catch (Exception e) {
            return CommonResult.failed("系统错误: " + e.getMessage());
        }
    }

    // 2. 获取申请列表
    @GetMapping("/list")
    public CommonResult<List<Apply>> list(@RequestParam String role, @RequestParam Long userId) {
        QueryWrapper<Apply> query = new QueryWrapper<>();

        // ... (保持你原来的查询逻辑不变，只需把返回值包一层) ...
        if ("student".equals(role)) {
            query.eq("user_id", userId);
        } else if ("teacher".equals(role)) {
            List<User> students = userService.list(new QueryWrapper<User>().eq("teacher_id", userId));
            List<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toList());
            if (studentIds == null) studentIds = new ArrayList<>();
            studentIds.add(userId);
            query.in("user_id", studentIds);
            query.last("ORDER BY CASE WHEN status = 0 THEN 0 ELSE 1 END, create_time DESC");
        } else if ("admin".equals(role)) {
            query.ge("status", 1);
            query.last("ORDER BY CASE WHEN status = 1 THEN 0 ELSE 1 END, create_time DESC");
        }

        return CommonResult.success(applyService.list(query));
    }

    // 3. 审批
    @PostMapping("/audit")
    public CommonResult<String> audit(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Integer result = Integer.valueOf(params.get("result").toString());
            String opinion = (String) params.get("opinion");
            String role = (String) params.get("role");

            Apply apply = applyService.getById(id);
            if (apply == null) return CommonResult.failed("申请不存在");

            if ("teacher".equals(role)) {
                apply.setStatus(result == 1 ? 1 : 3);
                apply.setTeacherAuditOpinion(opinion);
                apply.setTeacherAuditTime(LocalDateTime.now());
                applyService.updateById(apply);
            } else if ("admin".equals(role)) {
                if (result == 1) {
                    Long adminId = UserContext.getUser() != null ? UserContext.getUser().getId() : 1L;
                    applyService.approveApply(id, adminId); // 调用你的业务逻辑
                    // 重新获取对象以确保状态正确
                    apply = applyService.getById(id);
                } else {
                    apply.setStatus(4);
                }
                apply.setAdminAuditOpinion(opinion);
                apply.setAdminAuditTime(LocalDateTime.now());
                applyService.updateById(apply);
            }
            return CommonResult.success("审批成功");
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResult.failed("审批失败: " + e.getMessage());
        }
    }
}