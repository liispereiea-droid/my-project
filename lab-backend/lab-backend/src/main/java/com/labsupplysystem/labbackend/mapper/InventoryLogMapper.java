package com.labsupplysystem.labbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labsupplysystem.labbackend.entity.InventoryLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryLogMapper extends BaseMapper<InventoryLog> {
}