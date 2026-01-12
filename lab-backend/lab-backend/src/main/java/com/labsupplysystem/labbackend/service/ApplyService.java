package com.labsupplysystem.labbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.labsupplysystem.labbackend.entity.Apply;

public interface ApplyService extends IService<Apply> {
    boolean submitApply(Apply apply);
}