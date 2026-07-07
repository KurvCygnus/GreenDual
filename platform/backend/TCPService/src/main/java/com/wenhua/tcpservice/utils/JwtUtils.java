package com.wenhua.tcpservice.utils;

import com.wenhua.tcpservice.config.JwtConfigInitializer;
import com.wenhua.tcpservice.pojo.PermissionLevel;
import com.wenhua.tcpservice.pojo.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;

import javax.crypto.spec.SecretKeySpec;
import java.lang.StackWalker.Option;
import java.security.Key;
import java.util.Date;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @see JwtConfigInitializer
 */
public final class JwtUtils
{
    private JwtUtils() { throw new IllegalAccessError("Class \"JwtUtils\" is not meant to be instantized!"); }
    
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
    
    public static final Function<String, Key> STR_2_KEY = key -> new SecretKeySpec(HexFormat.of().parseHex(key), "HmacSHA512");
    
    /**
     * @implNote 默认密钥使用 SHA-512 生成. 可通过 {@link #setSignKey(String)} 在应用启动时注入
     * (从 {@code application.properties} 的 {@code jwt.sign-key} 或环境变量 {@code JWT_SIGN_KEY} 读取).
     */
    private static Key SIGN_KEY =
        //! 该值用于fallback, 除非Jwt在Spring Boot的初始化周期尚未完成时就被调用, 否则该值永远都不会被使用.
        STR_2_KEY.apply("c9c3f12534894b8cbb94d61971415cd80145a3e6d5c8e2748161798abc9e1cc69ddc7418f1d252d6cfd94a6bad0a5674fe270794ea5d1c82d58f3a130210bc6f");
    private static long EXPIRE = 43200000L;
    private static long REFRESH_EXPIRE = 604800000L; // 等于 7 天

    /**
     * 在应用启动时由 Spring 调用，将配置的签名密钥注入到静态字段.
     *
     * @param signKey 签名密钥，来自 {@code jwt.sign-key}
     */
    public static void setSignKey(String signKey)
    {
        //! 防止某些小可爱乱改
        if(!STACK_WALKER.getCallerClass().equals(JwtConfigInitializer.class))
            throw new UnsupportedOperationException();
        
        if(signKey != null && !signKey.isBlank())
            JwtUtils.SIGN_KEY = STR_2_KEY.apply(signKey);
    }
    
    /**
     * 在应用启动时由 Spring 调用，将配置的过期时间注入到静态字段.
     *
     * @param expire 过期时间（毫秒）
     */
    public static void setExpire(long expire)
    {
        //! 防止某些小可爱乱改
        if(!STACK_WALKER.getCallerClass().equals(JwtConfigInitializer.class))
            throw new UnsupportedOperationException();

        if(expire > 0)
            JwtUtils.EXPIRE = expire;
    }

    /**
     * 在应用启动时由 Spring 调用，将配置的刷新令牌过期时间注入到静态字段.
     *
     * @param refreshExpire 刷新令牌过期时间（毫秒）
     */
    public static void setRefreshExpire(long refreshExpire)
    {
        //! 防止某些小可爱乱改
        if(!STACK_WALKER.getCallerClass().equals(JwtConfigInitializer.class))
            throw new UnsupportedOperationException();

        if(refreshExpire > 0)
            JwtUtils.REFRESH_EXPIRE = refreshExpire;
    }
    
    //? 额, 职责不太清晰. 但反正这不是我的项目, 也比四个Controller里重复写isAdmin好 UwU.
    public static boolean isAdmin(@NonNull HttpServletRequest request, @NonNull Function<? super Integer, User> userTransformer)
    {
        return getUserId(request).
            map(userTransformer).
            //! 不需要使用`filter(Objects::nonNull)`. [[Optional]]内部会处理返回值为`null`的情况, 返回[[Optional#empty]].
            map(user -> user.getPermissionLevel().hasPermission(PermissionLevel.ADMIN)).
            orElse(false);
    }
    
    public static Optional<Integer> getUserId(HttpServletRequest request) { return getUserId(request.getHeader("token")); }
    
    public static Optional<Integer> getUserId(String token)
    {
        try
        {
            final var claims = Jwts.parserBuilder().
                setSigningKey(JwtUtils.SIGN_KEY).
                build().
                parseClaimsJws(token).
                getBody();
            
            return Optional.ofNullable(claims.get("userId", Integer.class));
        }
        catch(Exception e)
        {
            // 可以设置响应状态码和消息，表示令牌无效
            //response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return Optional.empty();
        }
    }
    
    
    /**
     * 生成JWT令牌
     * @param claims JWT第二部分负载 payload 中存储的内容
     */
    @Nonnull public static String generateJwt(@Nonnull Map<String, Object> claims)
    {
        return Jwts.builder().
            addClaims(claims).
            signWith(SIGN_KEY, SignatureAlgorithm.HS256).
            setExpiration(new Date(System.currentTimeMillis() + EXPIRE)).
            compact();
    }
    
    /**
     * 解析JWT令牌
     *
     * @param jwt JWT令牌
     * @return JWT第二部分负载 payload 中存储的内容
     */
    @Nonnull public static Claims parseJWT(@Nonnull String jwt)
    {
        return Jwts.parserBuilder().
            setSigningKey(SIGN_KEY).
            build().
            parseClaimsJws(jwt).
            getBody();
    }

    /**
     * 生成刷新令牌（Refresh Token），有效期比 Access Token 长.
     *
     * @param claims 令牌中存储的声明（建议至少包含 userId）
     * @return 刷新令牌字符串
     */
    @Nonnull public static String generateRefreshToken(@Nonnull Map<String, Object> claims)
    {
        //! 标记此令牌为 refresh token，防止被误当作 access token 使用
        claims.put("purpose", "refresh");
        return Jwts.builder().
            addClaims(claims).
            signWith(SIGN_KEY, SignatureAlgorithm.HS256).
            setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRE)).
            compact();
    }

    /**
     * 解析并验证刷新令牌.
     *
     * @param token 刷新令牌字符串
     * @return 令牌中包含的 claims
     * @throws io.jsonwebtoken.ExpiredJwtException 令牌已过期
     * @throws JwtException 签名无效或格式错误
     */
    @Nonnull public static Claims parseRefreshToken(@Nonnull String token)
    {
        final var claims = Jwts.parserBuilder().
            setSigningKey(SIGN_KEY).
            build().
            parseClaimsJws(token).
            getBody();

        if(!"refresh".equals(claims.get("purpose", String.class)))
            throw new JwtException("Token is not a refresh token");

        return claims;
    }

    /**
     * 判断给定的 token 是否为刷新令牌.
     */
    public static boolean isRefreshToken(@Nonnull String token)
    {
        try
        {
            final var claims = Jwts.parserBuilder().
                setSigningKey(SIGN_KEY).
                build().
                parseClaimsJws(token).
                getBody();

            return "refresh".equals(claims.get("purpose", String.class));
        }
        catch(Exception e) { return false; }
    }
}
