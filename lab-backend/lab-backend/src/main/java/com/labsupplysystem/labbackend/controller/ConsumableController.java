package com.labsupplysystem.labbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.labsupplysystem.labbackend.common.api.CommonResult;
import com.labsupplysystem.labbackend.entity.Consumable;
import com.labsupplysystem.labbackend.service.ConsumableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.labsupplysystem.labbackend.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/consumable")
@CrossOrigin
public class ConsumableController {

    @Autowired
    private ConsumableService consumableService;

    // 1. 获取耗材列表
    @GetMapping("/list")
    public CommonResult<List<Consumable>> list(@RequestParam(required = false) String name) {
        QueryWrapper<Consumable> wrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like("consumable_name", name);
        }
        // 按时间倒序，新加的排前面
        wrapper.orderByDesc("create_time");
        return CommonResult.success(consumableService.list(wrapper));
    }

    // 2. 新增耗材 (已修复 category_id 报错)
    @PostMapping("/add")
    public CommonResult<String> add(@RequestBody Consumable consumable) {
        try {
            // === 核心修复开始 ===
            // 1. 如果没传分类ID，默认为1 (避免 Field 'category_id' doesn't have a default value)
            if (consumable.getCategoryId() == null) {
                consumable.setCategoryId(1);
            }

            // 2. 如果没传实验室ID，默认为1
            if (consumable.getLabId() == null) {
                consumable.setLabId(1);
            }

            // 3. 补全库存和预警值的默认值
            if (consumable.getCurrentStock() == null) consumable.setCurrentStock(0);
            if (consumable.getSafetyStock() == null) consumable.setSafetyStock(10);

            // 4. 初始化预测字段，防止数据库报错
            consumable.setPredictedUsage30d(0);
            consumable.setDynamicThreshold(0);
            // === 核心修复结束 ===

            consumable.setCreateTime(LocalDateTime.now());
            consumable.setStatus(1); // 默认启用

            boolean success = consumableService.save(consumable);
            if (success) {
                return CommonResult.success("新增成功");
            } else {
                return CommonResult.failed("新增失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 把具体的错误信息返回给前端，方便调试
            return CommonResult.failed("系统错误: " + e.getMessage());
        }
    }

    // 3. 修改耗材
    @PostMapping("/update")
    public CommonResult<String> update(@RequestBody Consumable consumable) {
        consumable.setUpdateTime(LocalDateTime.now());
        boolean success = consumableService.updateById(consumable);
        return success ? CommonResult.success("修改成功") : CommonResult.failed("修改失败");
    }

    // 4. 删除耗材
    @PostMapping("/delete/{id}")
    public CommonResult<String> delete(@PathVariable Integer id) {
        boolean success = consumableService.removeById(id);
        return success ? CommonResult.success("删除成功") : CommonResult.failed("删除失败");
    }

    @Autowired
    private PredictionService predictionService;

    /**
     * 手动触发某项耗材的智能预测
     * 访问路径例如：GET /consumable/predict/1
     */
    @GetMapping("/predict/{id}")
    public CommonResult<String> generatePrediction(@PathVariable("id") Integer id) {
        boolean success = predictionService.runPredictionForConsumable(id);
        if (success) {
            return CommonResult.success("预测成功，库存预警线已动态更新！");
        } else {
            return CommonResult.failed("预测失败，请检查算法微服务状态。");
        }
    }

}