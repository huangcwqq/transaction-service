package com.hsbc.transaction.util;

import com.hsbc.transaction.common.TransactionErrors;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 防重和防非法请求token的生成和校验
 */
public class TokenUtil {

    // 用于存储token相关信息
    public record TokenInfo(LocalDateTime createAt,Boolean isUse) {
    }

    private static final ConcurrentHashMap<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();

    /**
     * 校验防重token是否有效，并标记为已使用
     * @param token 防重token
     *
     */
    public static void validateAndConsumeToken(String token) {
        // 防重token不能为空
        if(token == null){
            throw TransactionErrors.illegalRequest();
        }
        TokenInfo tokenInfo = tokenStore.get(token);
        // 非法请求
        if(tokenInfo == null){
            throw TransactionErrors.illegalRequest();
        }
        // 判断是否超时过期,过期时间设置为5秒，便于测试
        if(tokenInfo.createAt().plusSeconds(5).isBefore(LocalDateTime.now())){
            tokenStore.remove(token);
            throw TransactionErrors.invalidToken();
        }
        // 判断是否已使用
        if(tokenInfo.isUse()){
            throw TransactionErrors.duplicateRequest();
        }
        tokenStore.put(token, new TokenInfo(tokenInfo.createAt(), true));
    }

    /**
     * 生成防重token
     * @return 防重token
     */
    public static String generateToken() {
        String token = UUID.randomUUID().toString().replace("-", "");
        TokenInfo tokenInfo = new TokenInfo(LocalDateTime.now(), false);
        tokenStore.put(token, tokenInfo);
        return token;
    }
}
