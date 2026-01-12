package com.labsupplysystem.labbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_apply")
public class Apply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;          // 申请人ID
    private String userName;      // 申请人名字
    private Integer labId;        // 实验室ID
    private Integer consumableId; // 耗材ID
    private String consumableName;// 耗材名字
    private Integer applyCount;   // 申请数量
    private String reason;        // 申请理由

    // 状态: 0=待导审, 1=待管审, 2=已通过, 3=导师驳回, 4=管理员驳回
    private Integer status;
    private LocalDateTime createTime;

    // === 新增以下字段 (解决报错的核心) ===
    private String teacherAuditOpinion;   // 导师意见
    private LocalDateTime teacherAuditTime; // 导师审批时间
    private String adminAuditOpinion;     // 管理员意见
    private LocalDateTime adminAuditTime;   // 管理员审批时间
}