package com.leo.shorturl.service.impl;

import com.leo.shorturl.constants.RedisKeyConstants;
import com.leo.shorturl.domain.dto.LongURL;
import com.leo.shorturl.domain.po.UrlMapping;
import com.leo.shorturl.event.UrlAccessEvent;
import com.leo.shorturl.exception.ShortUrlNotFoundException;
import com.leo.shorturl.mapper.UrlMappingMapper;
import com.leo.shorturl.service.IUrlMappingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leo.shorturl.utils.JsonUtils;
import com.leo.shorturl.utils.ShortUrlUtils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 短链接与原始链接映射表 服务实现类
 * </p>
 *
 * @author leo
 * @since 2025-09-06
 */
@Service
@RequiredArgsConstructor
public class UrlMappingServiceImpl extends ServiceImpl<UrlMappingMapper, UrlMapping> implements IUrlMappingService {
    private static final int MAX_RETRIES = 5; // 定义最大重试次数以防哈希碰撞
    private final StringRedisTemplate redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建短链接
     * @param longURL 包含长链接和可选的过期时间
     * @return 生成的短链接码
     */
    @Override
    public String createShortUrl(LongURL longURL) {
        // 1. 获取长URL和过期时间
        String longUrl = longURL.getLongUrl();

        // 2. 查询是否存在有效的映射（未过期且启用的）
        UrlMapping existing = lambdaQuery()
                .eq(UrlMapping::getLongUrl, longUrl)
                .last("LIMIT 1")
                .one();
        // 2.如果存在则更新返回
        if (existing != null) {
           existing.setExpiresAt(longURL.getExpiresAt()); // 使用新的过期时间（可以为null）
           existing.setIsEnabled(true); // 重新启用
           updateById(existing);
           String shortCode = existing.getShortCode();
           // 删除缓存，确保下次访问时使用最新数据
           redisTemplate.delete(RedisKeyConstants.SHORT_URL_KEY_PREFIX + shortCode);
           return shortCode;
        }
        // 3. 不存在则创建新的映射
        return createNewMapping(longURL);
    }

    /**
     * 获取短链接对应的长链接并重定向
     * @param shortCode 短链接码
     * @return 重定向视图
     */
    @Override
    public RedirectView getShortUrl(String shortCode) {
        // 1.查询缓存
        String key= RedisKeyConstants.SHORT_URL_KEY_PREFIX + shortCode;
        String cachedData = redisTemplate.opsForValue().get(key);
        UrlMapping map;
        // 1.1 如果缓存存在直接返回
        if (cachedData != null) {
            map=JsonUtils.fromJson(cachedData, UrlMapping.class);
        }else {
            // 2. 缓存不存在查询数据库
            map = lambdaQuery()
                    .eq(UrlMapping::getShortCode, shortCode)
                    .eq(UrlMapping::getIsEnabled,true)
                    .gt(UrlMapping::getExpiresAt, LocalDateTime.now())
                    .last("LIMIT 1")
                    .one();
            // 2.1 如果不存在则返回404页面
            if (map == null) {
                throw new ShortUrlNotFoundException("Short URL not found for code: " + shortCode);
            }
            // 2.2 存在则写入缓存并返回
            if (map.getExpiresAt() != null) {
                // 计算过期时间
                long seconds = java.time.Duration.between(LocalDateTime.now(), map.getExpiresAt()).getSeconds();
                redisTemplate.opsForValue().set(key, JsonUtils.toJson(map), seconds, TimeUnit.SECONDS);
            }else {
                redisTemplate.opsForValue().set(key,JsonUtils.toJson(map));
            }
        }
        eventPublisher.publishEvent(new UrlAccessEvent(shortCode));
        return getRedirectView(map.getLongUrl());
    }

    private String createNewMapping(LongURL longURL) {
        // 1. 创建新的 UrlMapping 实例
        String longUrl = longURL.getLongUrl();
        UrlMapping newMapping = new UrlMapping();
        newMapping.setLongUrl(longUrl);
        newMapping.setExpiresAt(longURL.getExpiresAt());
        newMapping.setIsEnabled(true);

        // 2. 循环生成和检查，以处理极小概率的哈希碰撞事件
        for (int i = 0; i < MAX_RETRIES; i++) {
            // 2.1 通过添加重试次数作为 "salt" 来避免确定性哈希的重复碰撞
            String shortCode = ShortUrlUtils.generateShortUrl(longUrl, i);
            // 2.2 检查生成的 shortCode 是否已被其他 longUrl 占用
            boolean exists = lambdaQuery().eq(UrlMapping::getShortCode, shortCode).exists();
            if (!exists) {
                // 3. 如果 shortCode 未被占用，则设置并保存
                newMapping.setShortCode(shortCode);
                // 3.1 存入缓存
                String key = RedisKeyConstants.SHORT_URL_KEY_PREFIX + shortCode;
                Long expireSeconds = newMapping.getExpiresAt() == null ? null :
                        java.time.Duration.between(LocalDateTime.now(), newMapping.getExpiresAt()).getSeconds();
                if (expireSeconds != null) {
                    redisTemplate.opsForValue().set(key, JsonUtils.toJson(newMapping), expireSeconds);
                } else {
                    redisTemplate.opsForValue().set(key, JsonUtils.toJson(newMapping));
                }
                // 3.2 保存到数据库
                save(newMapping);
                // 3.3 返回生成的短链接码
                return shortCode;
            }
        }
        // 2.3 如果重试多次后仍然失败，则抛出异常
        throw new RuntimeException("Failed to generate a unique short URL for: " + longUrl);
    }

    /**
     * 创建重定向视图
     * @param url 重定向的URL
     * @return  重定向视图
     */
    private RedirectView getRedirectView(String url) {
        RedirectView redirectView = new RedirectView(url);
        redirectView.setStatusCode(HttpStatus.FOUND);
        return redirectView;
    }
    private RedirectView getUnFoundView() {
        RedirectView redirectView = new RedirectView("/");
        redirectView.setStatusCode(HttpStatus.NOT_FOUND);
        return redirectView;
    }
}
