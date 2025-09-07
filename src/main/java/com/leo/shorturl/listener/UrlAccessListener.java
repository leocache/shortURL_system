package com.leo.shorturl.listener;

import com.leo.shorturl.constants.RedisKeyConstants;
import com.leo.shorturl.event.UrlAccessEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class UrlAccessListener {
    private final StringRedisTemplate redisTemplate;

    @Async
    @EventListener(UrlAccessEvent.class)
    public void handlerUrlAccess(UrlAccessEvent event) {
        String key= RedisKeyConstants.URL_CLICK_COUNT_PREFIX+event.getShortCode();
        redisTemplate.opsForValue().increment(key, 1);
        log.debug("Increment click count for shortCode:{}", event.getShortCode());
    }
}
