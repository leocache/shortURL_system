package com.leo.shorturl.controller;


import com.leo.shorturl.domain.dto.LongURL;
import com.leo.shorturl.service.IUrlMappingService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

/**
 * <p>
 * 短链接与原始链接映射表 前端控制器
 * </p>
 *
 * @author leo
 * @since 2025-09-06
 */
@RestController
@RequestMapping("/url-mapping")
@AllArgsConstructor
public class UrlMappingController {
    private final IUrlMappingService urlMappingService;
    @PostMapping("/create")
    public String createShortUrl(@RequestBody LongURL longURL) {
        return urlMappingService.createShortUrl(longURL);
    }
    @GetMapping("{shortCode}")
    public RedirectView getShortUrl(@PathVariable("shortCode") String shortCode) {
        return urlMappingService.getShortUrl(shortCode);
    }
}
