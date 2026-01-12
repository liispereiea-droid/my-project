package com.labsupplysystem.labbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.labsupplysystem.labbackend.entity.Consumable;
import com.labsupplysystem.labbackend.service.ConsumableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/consumable")
@CrossOrigin
public class ConsumableController {

    @Autowired
    private ConsumableService consumableService;

    // 1. 获取列表 (支持搜索)
    @GetMapping("/list")
    public List<Consumable> getList(@RequestParam(defaultValue = "1") Integer labId,
                                    @RequestParam(required = false) String name) {
        QueryWrapper<Consumable> query = new QueryWrapper<>();
        query.eq("lab_id", labId);
        if (name != null && !name.isEmpty()) {
            query.like("name", name);
        }
        query.orderByDesc("id"); // 新加的排前面
        return consumableService.list(query);
    }

    // 2. === 新增：添加耗材 ===
    @PostMapping("/add")
    public String add(@RequestBody Consumable consumable) {
        consumable.setUpdateTime(LocalDateTime.now());
        // 如果没填实验室ID，默认1
        if (consumable.getLabId() == null) consumable.setLabId(1);
        boolean save = consumableService.save(consumable);
        return save ? "success" : "error";
    }

    // 3. === 新增：更新耗材 (修改库存、价格等) ===
    @PutMapping("/update")
    public String update(@RequestBody Consumable consumable) {
        consumable.setUpdateTime(LocalDateTime.now());
        boolean update = consumableService.updateById(consumable);
        return update ? "success" : "error";
    }

    // 4. === 新增：删除耗材 ===
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        boolean remove = consumableService.removeById(id);
        return remove ? "success" : "error";
    }
}