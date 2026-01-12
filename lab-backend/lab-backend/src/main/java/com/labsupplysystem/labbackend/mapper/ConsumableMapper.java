package com.labsupplysystem.labbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labsupplysystem.labbackend.entity.Consumable;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConsumableMapper extends BaseMapper<Consumable> {
}