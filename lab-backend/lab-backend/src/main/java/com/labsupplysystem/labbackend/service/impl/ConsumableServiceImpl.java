package com.labsupplysystem.labbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.labsupplysystem.labbackend.entity.Consumable;
import com.labsupplysystem.labbackend.mapper.ConsumableMapper;
import com.labsupplysystem.labbackend.service.ConsumableService;
import org.springframework.stereotype.Service;

@Service
public class ConsumableServiceImpl extends ServiceImpl<ConsumableMapper, Consumable> implements ConsumableService {
}