package com.wenhua.tcpservice.utils;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import org.slf4j.helpers.MessageFormatter;

/**
 * 基于 SLF4J {@link MessageFormatter#arrayFormat(String, Object[])} 的格式化字符串工具.
 * <p>
 * 使用 {@code {}} 占位符, 支持 {@code Throwable} 作为最后一个参数传递异常堆栈.
 * 替代 {@link String#format}/{@link String#formatted} 及字符串拼接.
 * <p>
 * 用法:
 * <pre>{@code
 *   StrFormatter.format("Hello, {}!", name);
 *   StrFormatter.format("{} = {}", key, value);
 *   StrFormatter.format("Error: {}", e.getMessage(), e);  // 最后一个 Throwable 会被特殊处理
 * }</pre>
 */
public final class StringFormatter
{
    private StringFormatter() { throw new IllegalAccessError("Utility class"); }

    /**
     * 使用 {@link MessageFormatter#arrayFormat} 格式化字符串.
     *
     * @param pattern 包含 {@code {}} 占位符的模式字符串
     * @param args    可变参数, 替换占位符
     * @return 格式化后的字符串, 如果 {@code pattern} 为 {@code null} 则返回 {@code null}
     */
    public static @NonNull String format(@Nonnull String pattern, @Nullable Object... args) { return MessageFormatter.arrayFormat(pattern, args).getMessage(); }
}