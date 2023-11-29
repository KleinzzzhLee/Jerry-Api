package com.api.project.filter;

import com.api.common.model.entity.User;
import com.api.project.constant.UserConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class UserFilter implements Filter {

    private final RedisTemplate redisTemplate;

    private Set<String> excludePath;

    public UserFilter(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.excludePath = new HashSet<>();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("过滤器已启动");
        excludePath.add("/api/user/login");
        excludePath.add("/api/user/register");
        excludePath.add("/api/user/updatePassword");
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        request
        String requestURI = request.getRequestURI();
        if(excludePath.contains(requestURI)) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            // todo 没有登录态， 跳转登录页面
            User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
            if(user == null) {
                String contextPath = request.getContextPath();
                response.sendRedirect(contextPath + "/user/login");
            } else {
                filterChain.doFilter(servletRequest, servletResponse);

                // 在目标方法之后执行
                // todo 从redis中获取到用户的信息
                Map map = redisTemplate.opsForHash().entries(user.getId().toString());
                if(map.isEmpty()) {
                    response.sendRedirect(request.getContextPath() + "/user/login");
                }
                else {
                    Integer leaveTimes = (Integer) map.get("leaveTimes");
                    if (leaveTimes == 0) {
                        redisTemplate.delete(user.getId().toString());
                    } else {
//                    leaveTimes -= 1;
//                    Map<String, Object> newMap = new HashMap<>();
//                    map.put("leaveTimes", leaveTimes);
//                    redisTemplate.opsForSet().add(String.valueOf(user.getId()), leaveTimes);
//                    redisTemplate.expire(String.valueOf(user.getId()), 15 * 60, TimeUnit.SECONDS);
                        redisTemplate.opsForHash().increment(String.valueOf(user.getId()),"leaveTimes", -1);
                    }
                }

            }
        }

    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
