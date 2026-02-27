package com.labsupplysystem.labbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.labsupplysystem.labbackend.entity.Apply;
import com.labsupplysystem.labbackend.entity.Consumable;
import com.labsupplysystem.labbackend.entity.InventoryLog;
import com.labsupplysystem.labbackend.mapper.ApplyMapper;
import com.labsupplysystem.labbackend.mapper.ConsumableMapper;
import com.labsupplysystem.labbackend.mapper.InventoryLogMapper;
import com.labsupplysystem.labbackend.service.ApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ApplyServiceImpl extends ServiceImpl<ApplyMapper, Apply> implements ApplyService {

    @Autowired
    private ConsumableMapper consumableMapper;

    @Autowired
    private InventoryLogMapper inventoryLogMapper; // 之前让你新建的那个 Mapper

    @Override
    public boolean submitApply(Apply apply) {
        // 设置初始状态：0 (待导师审批)
        apply.setStatus(0);
        apply.setCreateTime(LocalDateTime.now());
        return this.save(apply);
    }

    /**
     * 管理员审批通过逻辑
     * 1. 修改申请单状态
     * 2. 扣减耗材库存
     * 3. 记录库存变动流水 (为算法积累数据)
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务，保证这三步要么全成功，要么全失败
    public void approveApply(Long applyId, Long approverId) {
        // 1. 获取申请单
        Apply apply = this.getById(applyId);
        if (apply == null) {
            throw new RuntimeException("申请单不存在");
        }
        if (apply.getStatus() == 2) {
            throw new RuntimeException("该单据已审批通过，请勿重复操作");
        }

        // 2. 更新申请单状态为 "已通过"
        apply.setStatus(2);
        apply.setApproverId(approverId);
        apply.setApproveDate(LocalDateTime.now());
        this.updateById(apply);

        // 3. 处理库存扣减
        Consumable consumable = consumableMapper.selectById(apply.getConsumableId());
        if (consumable == null) {
            throw new RuntimeException("耗材信息不存在");
        }

        int oldStock = consumable.getCurrentStock();
        int useCount = apply.getApplyCount(); // 申请数量

        if (oldStock < useCount) {
            throw new RuntimeException("库存不足，当前仅剩: " + oldStock);
        }

        // 更新库存
        consumable.setCurrentStock(oldStock - useCount);
        consumableMapper.updateById(consumable);

        // ==========================================================
        // 4. 【核心逻辑】 写入库存流水表 (给算法喂数据)
        // ==========================================================
        InventoryLog log = new InventoryLog();
        log.setConsumableId(apply.getConsumableId());
        log.setChangeAmount(-useCount);  // 出库记为负数
        log.setChangeType(2);            // 2 代表“领用出库”
        log.setAfterBalance(oldStock - useCount); // 记录变动后的余额
        log.setCreateTime(LocalDateTime.now());   // 记录当前时间

        inventoryLogMapper.insert(log);
    }
}