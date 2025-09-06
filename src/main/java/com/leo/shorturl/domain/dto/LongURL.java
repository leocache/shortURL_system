package com.leo.shorturl.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author leo
 * @since 2025-09-06
 */
@Data
public class LongURL {
    private String longUrl;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime expiresAt;
}
