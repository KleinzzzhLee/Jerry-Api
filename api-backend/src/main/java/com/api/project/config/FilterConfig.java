package com.api.project.config;

import com.api.project.filter.UserFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@Configuration
public class FilterConfig {

    @Resource
    private RedisTemplate redisTemplate;

    @Bean
    public FilterRegistrationBean<UserFilter> userFilterFilterRegistrationBean() {
        FilterRegistrationBean<UserFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new UserFilter(redisTemplate));
        bean.setName("userFilter");
        bean.addUrlPatterns("/user/*");
        bean.setOrder(0);
        System.out.println("userFilter已经注册");
        return bean;
    }
}
