package com.hmdp.interception;

import com.hmdp.utils.UserHolder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: SongZeyao
 * @Date: 2023/2/28 - 17:51
 * @Description:
 */
@Component
public class LoginInterception implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断ThreadLocal中是否有用户
        if (UserHolder.getUser() == null) {
            //如果没有，进行拦截
            response.setStatus(401);
            return false;
        }

        //如果有，则放行
        return true;
    }


}
