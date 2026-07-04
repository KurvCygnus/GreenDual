package com.wenhua.tcpservice.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.Map;

public final class JwtUtils
{
    private JwtUtils() { throw new IllegalAccessError("Class \"JwtUtils\" is not meant to be instantized!"); }

    /**
     * @implNote 默认密钥使用 SHA-512 生成. 可通过 {@link #setSignKey(String)} 在应用启动时注入
     *           (从 {@code application.properties} 的 {@code jwt.sign-key} 或环境变量 {@code JWT_SIGN_KEY} 读取).
     */
    private static String SIGN_KEY =
        "c9c3f12534894b8cbb94d61971415cd80145a3e6d5c8e2748161798abc9e1cc69ddc7418f1d252d6cfd94a6bad0a5674fe270794ea5d1c82d58f3a130210bc6f";
    private static long EXPIRE = 43200000L;

    /**
     * 在应用启动时由 Spring 调用，将配置的签名密钥注入到静态字段.
     * @param signKey 签名密钥，来自 {@code jwt.sign-key}
     */
    public static void setSignKey(String signKey) {
        if (signKey != null && !signKey.isBlank()) {
            JwtUtils.SIGN_KEY = signKey;
        }
    }

    /**
     * 在应用启动时由 Spring 调用，将配置的过期时间注入到静态字段.
     * @param expire 过期时间（毫秒）
     */
    public static void setExpire(long expire) {
        if (expire > 0) {
            JwtUtils.EXPIRE = expire;
        }
    }

    //通过令牌获取用户id方法
    public static Integer getUserId(HttpServletRequest request) { return getUserId(request.getHeader("token")); }
    
    //通过令牌获取用户id方法
    public static Integer getUserId(String token)
    {
        Integer userId;
        // 解析令牌,找出用户id
        try {
            // 解析 JWT
            Claims claims = Jwts.parser()
                    .setSigningKey(JwtUtils.SIGN_KEY) // 设置用于签名的密钥
                    .parseClaimsJws(token) // 解析 JWT
                    .getBody(); // 获取 JWT 中的 Claims

            // 假设 userId 存储在 JWT 的某个自定义 claim 中，这里以 "id" 为例
            // 注意：这里的 "id" 应该与您在 JWT 中设置 claim 时的键名相匹配
            userId = claims.get("userId", Integer.class); // 获取 userId

            // 现在您可以使用 userId 了
            //System.out.println("User ID from JWT:-=-=-=-=-=-=- " + userId2);


        } catch (Exception e) {
            // 可以设置响应状态码和消息，表示令牌无效
            //response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return -1; // 令牌无效，不继续处理请求
        }

        return userId;
    }


    /**
     * 生成JWT令牌
     * @param claims JWT第二部分负载 payload 中存储的内容
     * @return
     */
    @Nonnull
    public static String generateJwt(@Nonnull Map<String, Object> claims)
    {
        return Jwts.builder()
                .addClaims(claims)
                .signWith(SignatureAlgorithm.HS256, SIGN_KEY)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .compact();
    }

    /**
     * 解析JWT令牌
     * @param jwt JWT令牌
     * @return JWT第二部分负载 payload 中存储的内容
     */
    @Nonnull
    public static Claims parseJWT(@Nonnull String jwt)
    {
        return Jwts.parser()
                .setSigningKey(SIGN_KEY)
                .parseClaimsJws(jwt)
                .getBody();
    }
}
