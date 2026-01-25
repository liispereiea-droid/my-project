package com.labsupplysystem.labbackend.common.exception;

import com.labsupplysystem.labbackend.common.api.CommonResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public CommonResult handle(Exception e) {
        if (e instanceof RuntimeException) {
             // 这里可以打印堆栈日志，方便调试
            e.printStackTrace();
            return CommonResult.failed(e.getMessage());
        }
        e.printStackTrace();
        return CommonResult.failed("服务器内部错误，请联系管理员");
    }
}