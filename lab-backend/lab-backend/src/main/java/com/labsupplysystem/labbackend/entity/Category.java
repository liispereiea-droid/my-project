package com.labsupplysystem.labbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName; // 别忘了导包
import lombok.Data;
import java.time.LocalDateTime;

@Data

@TableName("sys_category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("category_name")
    private String categoryName;

    private LocalDateTime createTime;
}