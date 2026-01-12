package com.labsupplysystem.labbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user") // 对应数据库表名
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String role;      // admin, teacher, student
    private Long teacherId;   // 关联导师ID
    private Integer labId;    // 关联实验室ID
}