package com.leo.shorturl;

import com.leo.shorturl.utils.ShortUrlUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ShortUrlApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void test1() {
        String url="www.baidu.com";
        System.out.println(ShortUrlUtils.generateShortUrl(url,1));
    }

}
