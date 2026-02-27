package com.labsupplysystem.labbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.labsupplysystem.labbackend.entity.Apply;

public interface ApplyService extends IService<Apply> {
    // 原有的提交方法
    boolean submitApply(Apply apply);

    void approveApply(Long applyId, Long approverId);
}