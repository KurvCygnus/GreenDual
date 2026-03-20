package com.wenhua.tcpservice.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.Map;

public class JwtUtils {


    public static String signKey = "itheima";
    private static Long expire = 43200000L;

    //通过令牌获取用户id方法
    public static Integer getUserId(HttpServletRequest request){
        String token = request.getHeader("token");
        Integer userId = null;
        // 解析令牌,找出用户id
        try {
            // 解析 JWT
            Claims claims = Jwts.parser()
                    .setSigningKey(JwtUtils.signKey) // 设置用于签名的密钥
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
    //通过令牌获取用户id方法
    public static Integer getUserId(String token){

        Integer userId = null;
        // 解析令牌,找出用户id
        try {
            // 解析 JWT
            Claims claims = Jwts.parser()
                    .setSigningKey(JwtUtils.signKey) // 设置用于签名的密钥
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
    public static String generateJwt(Map<String, Object> claims){
        String jwt = Jwts.builder()
                .addClaims(claims)
                .signWith(SignatureAlgorithm.HS256, signKey)
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .compact();
        return jwt;
    }

    /**
     * 解析JWT令牌
     * @param jwt JWT令牌
     * @return JWT第二部分负载 payload 中存储的内容
     */
    public static Claims parseJWT(String jwt){
        Claims claims = Jwts.parser()
                .setSigningKey(signKey)
                .parseClaimsJws(jwt)
                .getBody();
        return claims;
    }
}
