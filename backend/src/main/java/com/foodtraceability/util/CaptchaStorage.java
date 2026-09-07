package com.foodtraceability.util;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CaptchaStorage {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final long EXPIRE_MINUTES = 5;
    
    public void setCaptcha(String key, String captcha) {
        String redisKey = "captcha:" + key.toLowerCase();
        redisTemplate.opsForValue().set(redisKey, captcha.toLowerCase(), 
                                        EXPIRE_MINUTES, TimeUnit.MINUTES);
    }
    
    public String getCaptcha(String key) {
        String redisKey = "captcha:" + key.toLowerCase();
        return redisTemplate.opsForValue().get(redisKey);
    }
    
    public void removeCaptcha(String key) {
        String redisKey = "captcha:" + key.toLowerCase();
        redisTemplate.delete(redisKey);
    }
}