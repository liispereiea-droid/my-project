package com.labsupplysystem.labbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.labsupplysystem.labbackend.entity.Category;
import com.labsupplysystem.labbackend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/category")
@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 列表查询
    @GetMapping("/list")
    public List<Category> list() {
        return categoryService.list(new QueryWrapper<Category>().orderByAsc("id"));
    }

    // 新增
    @PostMapping("/add")
    public String add(@RequestBody Category category) {
        return categoryService.save(category) ? "success" : "error";
    }

    // 修改
    @PutMapping("/update")
    public String update(@RequestBody Category category) {
        return categoryService.updateById(category) ? "success" : "error";
    }

    // 删除
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        return categoryService.removeById(id) ? "success" : "error";
    }
}