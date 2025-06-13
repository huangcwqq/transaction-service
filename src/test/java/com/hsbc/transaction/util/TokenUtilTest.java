package com.hsbc.transaction.util;

import com.hsbc.transaction.common.DuplicateRequestException;
import com.hsbc.transaction.common.InvalidRequestException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenUtilTest {

    // 每个测试用例前清空 tokenStore
    @BeforeEach
    void setUp() {
        TokenUtilTestHelper.clearTokenStore();
    }

    // 辅助类，用于访问 TokenUtil 的私有成员
    private static class TokenUtilTestHelper {
        public static void clearTokenStore() {
            TokenUtil.tokenStore.clear();
        }

        public static void putToken(String token, TokenUtil.TokenInfo tokenInfo) {
            TokenUtil.tokenStore.put(token, tokenInfo);
        }

        public static int getTokenStoreSize() {
            return TokenUtil.tokenStore.size();
        }
    }

    /**
     * TC01: 验证 generateToken 生成有效 token 并正确存储
     */
    @Test
    void testGenerateToken_ReturnsValidTokenAndStoresIt() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(now);

            String token = TokenUtil.generateToken();

            // 验证 token 不为空
            assertNotNull(token);
            assertFalse(token.isEmpty());

            // 验证 tokenStore 中存在该 token
            assertTrue(TokenUtil.tokenStore.containsKey(token));

            // 验证 TokenInfo 的内容
            TokenUtil.TokenInfo tokenInfo = TokenUtil.tokenStore.get(token);
            assertEquals(now, tokenInfo.createdAt());
            assertFalse(tokenInfo.used());
        }
    }

    /**
     * TC02: 验证多次调用 generateToken 生成不同的 token
     */
    @Test
    void testGenerateToken_MultipleCalls_ReturnDifferentTokens() {
        String token1 = TokenUtil.generateToken();
        String token2 = TokenUtil.generateToken();

        // 验证两次生成的 token 不同
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);

        // 验证 tokenStore 中有两个条目
        assertEquals(2, TokenUtilTestHelper.getTokenStoreSize());
    }

    /**
     * TC01: token 为 null
     */
    @Test
    void testValidateAndConsumeToken_NullToken_ThrowsInvalidRequestException() {
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> TokenUtil.validateAndConsumeToken(null)
        );
        assertEquals("非法请求！", exception.getMessage());
    }

    /**
     * TC02: token 不存在于 tokenStore
     */
    @Test
    void testValidateAndConsumeToken_TokenNotExists_ThrowsInvalidRequestException() {
        String token = "abc123";

        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> TokenUtil.validateAndConsumeToken(token)
        );
        assertEquals("无效的 token！", exception.getMessage());
    }

    /**
     * TC03: token 已过期
     */
    @Test
    void testValidateAndConsumeToken_TokenExpired_ThrowsInvalidRequestException() {
        String token = "abc123";
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
        TokenUtil.TokenInfo tokenInfo = new TokenUtil.TokenInfo(createdAt, false);
        TokenUtilTestHelper.putToken(token, tokenInfo);

        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 0, 1, 0); // 过期时间是 5 秒后
        try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(now);

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> TokenUtil.validateAndConsumeToken(token)
            );
            assertEquals("token 已过期！", exception.getMessage());

            // 确保 token 被移除了
            assertNull(TokenUtil.tokenStore.get(token));
        }
    }

    /**
     * TC04: token 已使用
     */
    @Test
    void testValidateAndConsumeToken_TokenAlreadyUsed_ThrowsDuplicateRequestException() {
        String token = "abc123";
        LocalDateTime createdAt = LocalDateTime.now().minusSeconds(1);
        TokenUtil.TokenInfo tokenInfo = new TokenUtil.TokenInfo(createdAt, true);
        TokenUtilTestHelper.putToken(token, tokenInfo);

        DuplicateRequestException exception = assertThrows(
                DuplicateRequestException.class,
                () -> TokenUtil.validateAndConsumeToken(token)
        );
        assertEquals("重复请求！", exception.getMessage());

        // 确保 token 仍存在但状态不变
        assertTrue(TokenUtil.tokenStore.get(token).used());
    }

    /**
     * TC05: token 有效且未使用
     */
    @Test
    void testValidateAndConsumeToken_ValidToken_SuccessfullyConsumed() {
        String token = "abc123";
        LocalDateTime createdAt = LocalDateTime.now().minusSeconds(1);
        TokenUtil.TokenInfo tokenInfo = new TokenUtil.TokenInfo(createdAt, false);
        TokenUtilTestHelper.putToken(token, tokenInfo);

        assertDoesNotThrow(() -> TokenUtil.validateAndConsumeToken(token));

        // 确保 token 被标记为已使用
        TokenUtil.TokenInfo updatedTokenInfo = TokenUtil.tokenStore.get(token);
        assertNotNull(updatedTokenInfo);
        assertTrue(updatedTokenInfo.used());
        assertEquals(createdAt, updatedTokenInfo.createdAt());
    }

}