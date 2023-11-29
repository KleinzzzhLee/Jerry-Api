package com.api.project.service.impl.inner;

import com.api.project.common.ErrorCode;
import com.api.project.exception.BusinessException;
import com.api.project.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.apache.dubbo.config.annotation.DubboService;
import com.api.common.model.entity.User;
import com.api.common.service.InnerUserService;

import javax.annotation.Resource;

@DubboService
public class InnerUserServiceImpl implements InnerUserService {
    /**
     *  获取对应的 user对象
     * @param appKey 相当于账号
     * @param appSecret 密码
     * @return
     */
    @Resource
    private UserService userService;

    @Override
    public User getInvokeUser(String appKey) {
        if(appKey == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("appKey", appKey);
        User user = userService.getOne(queryWrapper);
        if(user == null) {
            // todo 数据库中不存在该对象，如何向网关发送
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return user;
    }
}
