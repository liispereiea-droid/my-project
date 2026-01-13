package com.labsupplysystem.labbackend.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.labsupplysystem.labbackend.entity.Category;
import com.labsupplysystem.labbackend.mapper.CategoryMapper;
import com.labsupplysystem.labbackend.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {}