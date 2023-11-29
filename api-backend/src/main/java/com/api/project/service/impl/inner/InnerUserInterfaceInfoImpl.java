package com.api.project.service.impl.inner;

import com.api.project.service.UserInterfaceInfoService;
import org.apache.dubbo.config.annotation.DubboService;
import com.api.common.service.InnerUserInterfaceInfoService;
import org.springframework.data.annotation.Reference;

import javax.annotation.Resource;

@DubboService
public class InnerUserInterfaceInfoImpl implements InnerUserInterfaceInfoService {
    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;
    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        return userInterfaceInfoService.invoke(interfaceInfoId, userId);
    }
}
