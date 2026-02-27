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

    private Integer categoryId;    // 分类ID
    private String consumableName; // 耗材名称
    private String spec;           // 规格
    private String unit;           // 单位
    private BigDecimal price;      // 单价
    private String manufacturer;   // 厂家

    private Integer currentStock;  // 当前库存
    private Integer safetyStock;   // 警戒线


    private Integer labId;             // 归属实验室
    private Integer predictedUsage30d; // 预测消耗
    private Integer dynamicThreshold;  // 动态阈值
    private LocalDateTime lastPredictTime; // 上次预测时间

    private Integer status;        // 状态
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}