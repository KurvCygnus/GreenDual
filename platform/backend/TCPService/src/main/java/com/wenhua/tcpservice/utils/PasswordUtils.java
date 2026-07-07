package com.wenhua.tcpservice.utils;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 密码加密工具 — 使用 SHA-256 + 用户名盐值.
 * <p>
 * 格式: {@code SHA-256(username + "::GreenDual::" + rawPassword)} 的十六进制字符串 (64 字符).<br>
 * 兼容旧数据: 数据库中仍为明文的密码在验证通过后自动升级为哈希存储.
 */
public final class PasswordUtils
{
    private static final String SALT_PREFIX = "::GreenDual::";
    private static final char[] HEX = "0123456789abcdef".toCharArray();
    
    private static final MessageDigest SHA_256_ALGORITHM = initAlgorithm();
    
    private PasswordUtils() { throw new IllegalAccessError("Class \"PasswordUtils\" is not meant to be instantized!"); }

    /**
     * 对明文密码进行 SHA-256 加盐哈希.
     * @param rawPassword 明文密码
     * @param username    用户名（作为盐值的一部分）
     * @return 64 字符十六进制哈希字符串
     */
    @Nonnull public static String encode(@Nonnull String rawPassword, @Nonnull String username)
    {
        final var salted = username + SALT_PREFIX + rawPassword;
        final var hash = SHA_256_ALGORITHM.digest(salted.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    /**
     * 验证密码是否匹配.
     * <ol>
     *   <li><b>新格式 (v2):</b> 存储值为 {@code SHA-256(salt + rawPassword)}</li>
     *   <li><b>旧格式 (v1 — 明文):</b> 存储值直接为明文，向后兼容（首次登录后自动升级）</li>
     * </ol>
     * @param rawPassword     客户端传入的明文密码
     * @param username        用户名（盐值）
     * @param encodedPassword 数据库中存储的密码
     * @return 是否匹配
     */
    public static boolean matches(@Nullable String rawPassword, @NonNull String username, @Nullable String encodedPassword)
    {
        if(encodedPassword == null || rawPassword == null)
            return false;

        // 标准 v2 哈希比对
        if(encodedPassword.length() == 64 && encodedPassword.matches("[0-9a-f]{64}"))
            return encode(rawPassword, username).equals(encodedPassword);

        return rawPassword.equals(encodedPassword);
    }
    public static @NonNull String bytesToHex(byte @NonNull ... bytes)
    {
        final var hexChars = new char[bytes.length * 2];
        for(var i = 0; i < bytes.length; i++)
        {
            final var v = bytes[i] & 0xFF;
            hexChars[i * 2]     = HEX[v >>> 4];
            hexChars[i * 2 + 1] = HEX[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    private static @NonNull MessageDigest initAlgorithm()
    {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch(NoSuchAlgorithmException e) { throw new AssertionError("Impossible case"); }
    }
}