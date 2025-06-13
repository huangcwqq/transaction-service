package com.hsbc.transaction.util;

import com.hsbc.transaction.common.DuplicateRequestException;
import com.hsbc.transaction.common.InvalidRequestException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 防重和防非法请求token的生成和校验
 */
public class TokenUtil {

    // 用于记录token相关信息
    public record TokenInfo(LocalDateTime createdAt,Boolean used) {
    }
    // 用于存储token信息
    public static final ConcurrentHashMap<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();

    // 过期时间，这里设置为 5 秒，便于测试
    private static final long EXPIRE_TIME = 5;

    /**
     * 校验防重token是否有效，有效则标记为已使用
     * @param token 防重token
     *
     */
    public static void validateAndConsumeToken(String token) {
        // 防重token不能为空
        if(token == null){
            throw new InvalidRequestException("非法请求！");
        }
        TokenInfo tokenInfo = tokenStore.get(token);
        // 非法请求
        if(tokenInfo == null){
            throw new InvalidRequestException("无效的 token！");
        }
        // 判断token是否超时过期
        LocalDateTime expireTime = tokenInfo.createdAt().plusSeconds(EXPIRE_TIME);
        LocalDateTime now = LocalDateTime.now();
        if(now.isAfter(expireTime)){
            tokenStore.remove(token);
            throw new InvalidRequestException("token 已过期！");
        }
        // 判断是否已使用
        if(tokenInfo.used()){
            throw new DuplicateRequestException("重复请求！");
        }
        tokenStore.put(token, new TokenInfo(tokenInfo.createdAt(), true));
    }

    /**
     * 生成防重token
     * @return 防重token
     */
    public static String generateToken() {
        String token = UUID.randomUUID().toString();
        TokenInfo tokenInfo = new TokenInfo(LocalDateTime.now(), false);
        tokenStore.put(token, tokenInfo);
        return token;
    }
}
