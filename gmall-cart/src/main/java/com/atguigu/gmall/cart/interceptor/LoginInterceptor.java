package com.atguigu.gmall.cart.interceptor;

import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.cart.properties.JwtProperties;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    JwtProperties jwtProperties;
    @Autowired
    private static final ThreadLocal<UserInfo> THREAD_LOCAL=new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //获取userKey
        String userKey = CookieUtils.getCookieValue(request, this.jwtProperties.getUserKey());
        if(StringUtils.isBlank(userKey)){
            userKey = UUID.randomUUID().toString();
            //将未登录的userkey放入cookie中
            CookieUtils.setCookie(request,response,this.jwtProperties.getUserKey(),userKey,this.jwtProperties.getExpire());

        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserKey(userKey);

        //获取userId
        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());
        if(StringUtils.isNotBlank(token)){
            //解析jwt
            Map<String, Object> map = JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());
            Long userId = (Long)map.get("userId");
            userInfo.setUserId(userId);
        }
        THREAD_LOCAL.set(userInfo);
        return true;
    }

    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("后置方法");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("执行完方法");
        // 由于使用的是tomcat线程池，所有请求结束，线程并没有结束，只是回到了线程池，如果不手动释放资源，会导致内存泄漏
        THREAD_LOCAL.remove();
    }
}
