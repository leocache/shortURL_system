package com.leo.shorturl.controller;

import com.leo.shorturl.exception.ShortUrlNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public String handleShortUrlNotFound() {
        // 返回我们创建的404页面的路径 (Thymeleaf 会自动解析)
        return "error/404";
    }
}