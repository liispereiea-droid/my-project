package com.labsupplysystem.labbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_consumable")
public class Consumable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer labId;
    private String name;
    private String categoryName;
    private String spec;
    private String unit;
    private Integer currentStock;
    private Integer safetyStock;
    private BigDecimal price;
    private LocalDateTime updateTime;
}