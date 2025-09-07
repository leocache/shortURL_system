package com.leo.shorturl.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * URL访问事件
 */
@Data
@AllArgsConstructor
public class UrlAccessEvent {
    private String shortCode;
}
