package com.foodtraceability.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录尝试管理服务
 * 用于防止暴力破解攻击
 */
@Service
public class LoginAttemptService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String FAILED_ATTEMPTS_PREFIX = "login_failed:";
    private static final String LOCKED_ACCOUNT_PREFIX = "locked:";

    /** 最大失败尝试次数 */
    private static final int MAX_ATTEMPTS = 5;

    /** 账号锁定时间（30 分钟） */
    private static final long LOCK_TIME_MS = 30 * 60 * 1000;

    /**
     * 记录登录失败
     * @param username 用户名
     */
    public void loginFailed(String username) {
        String key = FAILED_ATTEMPTS_PREFIX + username;
        Long attemptsLong = redisTemplate.opsForValue().increment(key);
        int attempts = attemptsLong != null ? attemptsLong.intValue() : 1;
        // 设置 1 小时过期
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        // 如果达到最大尝试次数，锁定账号
        if (attempts >= MAX_ATTEMPTS) {
            lockAccount(username);
        }
    }

    /**
     * 检查账号是否被锁定
     * @param username 用户名
     * @return true-已锁定，false-未锁定
     */
    public boolean isLocked(String username) {
        String key = LOCKED_ACCOUNT_PREFIX + username;
        String lockTimeStr = redisTemplate.opsForValue().get(key);

        if (lockTimeStr != null) {
            long lockTime = Long.parseLong(lockTimeStr);
            long elapsed = System.currentTimeMillis() - lockTime;
            if (elapsed > LOCK_TIME_MS) {
                // 锁定期已过，自动解锁
                unlockAccount(username);
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 获取剩余锁定时间（秒）
     * @param username 用户名
     * @return 剩余秒数，0 表示未锁定
     */
    public long getRemainingLockTime(String username) {
        String key = LOCKED_ACCOUNT_PREFIX + username;
        String lockTimeStr = redisTemplate.opsForValue().get(key);

        if (lockTimeStr != null) {
            long lockTime = Long.parseLong(lockTimeStr);
            long elapsed = System.currentTimeMillis() - lockTime;
            long remaining = LOCK_TIME_MS - elapsed;
            return Math.max(0, remaining / 1000);
        }
        return 0;
    }

    /**
     * 登录成功，清除失败记录和锁定状态
     * @param username 用户名
     */
    public void loginSucceeded(String username) {
        String failedKey = FAILED_ATTEMPTS_PREFIX + username;
        String lockedKey = LOCKED_ACCOUNT_PREFIX + username;

        redisTemplate.delete(failedKey);
        redisTemplate.delete(lockedKey);
    }

    /**
     * 锁定账号
     * @param username 用户名
     */
    private void lockAccount(String username) {
        String key = LOCKED_ACCOUNT_PREFIX + username;
        redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 解锁账号
     * @param username 用户名
     */
    private void unlockAccount(String username) {
        String failedKey = FAILED_ATTEMPTS_PREFIX + username;
        String lockedKey = LOCKED_ACCOUNT_PREFIX + username;

        redisTemplate.delete(failedKey);
        redisTemplate.delete(lockedKey);
    }

    /**
     * 获取当前失败尝试次数
     * @param username 用户名
     * @return 失败次数
     */
    public int getFailedAttempts(String username) {
        String key = FAILED_ATTEMPTS_PREFIX + username;
        String val = redisTemplate.opsForValue().get(key);
        return val != null ? Integer.parseInt(val) : 0;
    }

    /**
     * 手动解锁账号（管理员使用）
     * @param username 用户名
     */
    public void forceUnlock(String username) {
        unlockAccount(username);
    }
}
