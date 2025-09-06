package com.leo.shorturl.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class ShortUrlUtils {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String generateShortUrl(String longUrl, int salt) {
        try {
            String urlToHash = longUrl + salt; // 添加盐值以增加随机性
            // 1. 使用SHA-256生成哈希
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(urlToHash.getBytes(StandardCharsets.UTF_8));

            // 2. 获取前16个字节作为熵源，并合并额外熵（时间戳 + UUID）
            long value = 0;
            for (int i = 0; i < 16; i++) {
                value = (value << 8) | (hash[i] & 0xFF);
            }

            // 合并额外熵源（时间戳 + UUID的低32位）
            long extraEntropy = System.currentTimeMillis() ^ UUID.randomUUID().getMostSignificantBits();
            value ^= extraEntropy;  // 异或操作，增加随机性

            // 3. 通过BASE62编码生成短链
            StringBuilder shortUrl = new StringBuilder();
            for (int i = 0; i < 12; i++) {
                shortUrl.append(BASE62.charAt((int) (Math.abs(value) % 62)));
                value /= 62;
            }

            // 4. 返回生成的短链（反转是为了确保从低位到高位映射）
            return shortUrl.reverse().toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating short URL", e);
        }
    }


}