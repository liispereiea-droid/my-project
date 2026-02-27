package com.labsupplysystem.labbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_inventory_log")
public class InventoryLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer consumableId;
    private Integer changeAmount; // 变动数量
    private Integer changeType;   // 1-入库, 2-出库, 3-盘点
    private Integer afterBalance; // 变动后余额
    private LocalDateTime createTime;
}