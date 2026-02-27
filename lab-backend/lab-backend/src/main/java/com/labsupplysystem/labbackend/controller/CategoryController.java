package com.labsupplysystem.labbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.labsupplysystem.labbackend.common.api.CommonResult; // 记得导包
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

    // 1. 列表查询 (改成 CommonResult)
    @GetMapping("/list")
    public CommonResult<List<Category>> list() {
        return CommonResult.success(categoryService.list(new QueryWrapper<Category>().orderByAsc("id")));
    }

    // 2. 新增
    @PostMapping("/add")
    public CommonResult<String> add(@RequestBody Category category) {
        return categoryService.save(category) ? CommonResult.success("新增成功") : CommonResult.failed("新增失败");
    }

    // 3. 修改
    @PutMapping("/update")
    public CommonResult<String> update(@RequestBody Category category) {
        return categoryService.updateById(category) ? CommonResult.success("修改成功") : CommonResult.failed("修改失败");
    }

    // 4. 删除
    @DeleteMapping("/delete/{id}")
    public CommonResult<String> delete(@PathVariable Integer id) {
        return categoryService.removeById(id) ? CommonResult.success("删除成功") : CommonResult.failed("删除失败");
    }
}