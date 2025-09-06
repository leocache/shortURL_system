package com.leo.shorturl.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 当此异常被抛出时，Spring 会自动返回 404 状态码
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ShortUrlNotFoundException extends RuntimeException {
    public ShortUrlNotFoundException(String message) {
        super(message);
    }
}