package com.labsupplysystem.labbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.labsupplysystem.labbackend.entity.Apply;
import com.labsupplysystem.labbackend.mapper.ApplyMapper;
import com.labsupplysystem.labbackend.service.ApplyService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ApplyServiceImpl extends ServiceImpl<ApplyMapper, Apply> implements ApplyService {
    @Override
    public boolean submitApply(Apply apply) {
        // 设置初始状态：0 (待导师审批)
        apply.setStatus(0);
        apply.setCreateTime(LocalDateTime.now());
        return this.save(apply);
    }
}