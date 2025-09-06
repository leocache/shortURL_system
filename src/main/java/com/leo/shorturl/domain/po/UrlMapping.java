package com.leo.shorturl.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 短链接与原始链接映射表
 * </p>
 *
 * @author leo
 * @since 2025-09-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("url_mapping")
public class UrlMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键，用于唯一标识和可能的短码生成
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 短链接唯一标识码
     */
    private String shortCode;

    /**
     * 原始长链接
     */
    private String longUrl;

    /**
     * 记录创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 链接过期时间, NULL 表示永不过期
     */
    private LocalDateTime expiresAt;

    /**
     * 链接点击次数统计
     */
    private Long clickCount;

    /**
     * 链接是否启用 (1: 启用, 0: 禁用)
     */
    private Boolean isEnabled;


}
