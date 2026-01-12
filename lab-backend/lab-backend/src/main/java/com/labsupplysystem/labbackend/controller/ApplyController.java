package com.labsupplysystem.labbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.labsupplysystem.labbackend.entity.Apply;
import com.labsupplysystem.labbackend.entity.Consumable;
import com.labsupplysystem.labbackend.entity.User;
import com.labsupplysystem.labbackend.service.ApplyService;
import com.labsupplysystem.labbackend.service.ConsumableService;
import com.labsupplysystem.labbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

    @Autowired
    private ConsumableService consumableService;

    // 1. 提交申请 (修复：根据角色判断初始状态)
    @PostMapping("/submit")
    public String submit(@RequestBody Apply apply) {
        try {
            // 查一下申请人的角色
            User user = userService.getById(apply.getUserId());

            if ("teacher".equals(user.getRole())) {
                // 如果是导师申请 -> 跳过导师审批，直接变成 1 (待管理员审批)
                apply.setStatus(1);
                apply.setTeacherAuditOpinion("系统自动通过(导师自申)");
                apply.setTeacherAuditTime(LocalDateTime.now());
            } else {
                // 如果是学生申请 -> 0 (待导师审批)
                apply.setStatus(0);
            }

            apply.setCreateTime(LocalDateTime.now());
            applyService.save(apply);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    // 2. 获取申请列表 (修复：查询历史记录，不再只看待办)
    @GetMapping("/list")
    public List<Apply> list(@RequestParam String role, @RequestParam Long userId) {
        QueryWrapper<Apply> query = new QueryWrapper<>();

        if ("student".equals(role)) {
            // 学生：看自己的所有记录 (不管状态是几)
            query.eq("user_id", userId);
        }
        else if ("teacher".equals(role)) {
            // 导师视角：
            // 1. 我名下学生的申请 (包括待办0 和 历史1,2,3,4)
            // 2. 我自己提交的申请 (user_id = me)

            List<User> students = userService.list(new QueryWrapper<User>().eq("teacher_id", userId));
            List<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toList());

            // 将导师自己的ID也加入查询列表，这样也能看到自己申请的单子
            if (studentIds == null) studentIds = new ArrayList<>();
            studentIds.add(userId);

            query.in("user_id", studentIds);

            // 排序优化：把 "待处理(0)" 的排在最前面，其他的按时间倒序
            query.last("ORDER BY CASE WHEN status = 0 THEN 0 ELSE 1 END, create_time DESC");
            return applyService.list(query);
        }
        else if ("admin".equals(role)) {
            // 管理员视角：
            // 看所有 status >= 1 的单子 (0是学生刚交的，管理员不需要看)
            // 这样既能看到待办(1)，也能看到历史(2,4)
            query.ge("status", 1);

            // 排序优化：把 "待处理(1)" 的排在最前面
            query.last("ORDER BY CASE WHEN status = 1 THEN 0 ELSE 1 END, create_time DESC");
        }

        return applyService.list(query);
    }

    // 3. 审批接口 (保持不变，含扣库存逻辑)
    @PostMapping("/audit")
    public String audit(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer result = Integer.valueOf(params.get("result").toString());
        String opinion = (String) params.get("opinion");
        String role = (String) params.get("role");

        Apply apply = applyService.getById(id);
        if (apply == null) return "申请不存在";

        if ("teacher".equals(role)) {
            if (result == 1) {
                apply.setStatus(1);
            } else {
                apply.setStatus(3);
            }
            apply.setTeacherAuditOpinion(opinion);
            apply.setTeacherAuditTime(LocalDateTime.now());
        }
        else if ("admin".equals(role)) {
            if (result == 1) {
                // 扣库存
                Consumable consumable = consumableService.getById(apply.getConsumableId());
                if (consumable.getCurrentStock() < apply.getApplyCount()) {
                    return "库存不足，无法通过审批！";
                }
                consumable.setCurrentStock(consumable.getCurrentStock() - apply.getApplyCount());
                consumableService.updateById(consumable);

                apply.setStatus(2); // 已完成
            } else {
                apply.setStatus(4); // 驳回
            }
            apply.setAdminAuditOpinion(opinion);
            apply.setAdminAuditTime(LocalDateTime.now());
        }

        applyService.updateById(apply);
        return "success";
    }
}