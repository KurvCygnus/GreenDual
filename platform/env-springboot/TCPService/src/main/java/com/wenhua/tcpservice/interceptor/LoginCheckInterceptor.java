package com.wenhua.tcpservice.interceptor;

import com.alibaba.fastjson.JSONObject;

import com.wenhua.tcpservice.pojo.Result;
import com.wenhua.tcpservice.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Enumeration;

@Component
@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {
    @Override//视图渲染完毕后执行...
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex)
    {
        System.out.println("afterCompletion");
    }

    @Override//资源方法运行后运行
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView)
    {
        System.out.println("postHandle");

    }

    //目标资源方法运行前运行,返回true:放行;返回false:不放行
    @Override
    public boolean preHandle(HttpServletRequest req, @NonNull HttpServletResponse resp, @NonNull Object handler) throws Exception {
        log.info("preHandle执行");

        //这里校验
        //1.获取请求url
        String url = req.getRequestURL().toString();
        log.info("请求的url:{}",url);
// 打印请求参数（对于GET请求）
        Enumeration<String> parameterNames = req.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            String[] parameterValues = req.getParameterValues(parameterName);
            StringBuilder values = new StringBuilder();
            for (String value : parameterValues) {
                if (!values.isEmpty()) {
                    values.append(", ");
                }
                values.append(value);
            }
            log.info("请求参数 {}: {}", parameterName, values);
        }

        //2.判断请求url中是否包含login,如果包含,说明是登录,放行
        if(url.contains("login")){
            log.info("登录请求,放行");
            return true;
        }

        //3.获取请求头中的令牌(token)
        String jwt = req.getHeader("token");
        log.info("jwt: {}", jwt);

        //4.判断令牌是否存在,如果不存在,返回错误
        if(!StringUtils.hasLength(jwt)){/*判断字符串是否有长度*/
            log.info("请求头token为空未登录");
            Result error = Result.error("NOT_LOGIN");
            //手动转换成json
            String notLoginStr = JSONObject.toJSONString(error);
            //写回去
            resp.getWriter().write(notLoginStr);
            //不放行
            return false;
        }

        //5.解析token,如果解析失败,返回错误---未登录
        try {
            JwtUtils.parseJWT(jwt);
        } catch (Exception e) {
            log.info("token解析失败");
            Result error = Result.error("NOT_LOGIN");
            String notLoginStr = JSONObject.toJSONString(error);
            resp.getWriter().write(notLoginStr);
            return false;
        }

        //6.放行
        log.info("令牌合法,放行");
        return true;
    }


}
