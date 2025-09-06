package com.leo.shorturl.service;

import com.leo.shorturl.domain.dto.LongURL;
import com.leo.shorturl.domain.po.UrlMapping;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.servlet.view.RedirectView;

/**
 * <p>
 * 短链接与原始链接映射表 服务类
 * </p>
 *
 * @author leo
 * @since 2025-09-06
 */
public interface IUrlMappingService extends IService<UrlMapping> {

    String createShortUrl(LongURL longURL);

    RedirectView getShortUrl(String shortCode);
}
