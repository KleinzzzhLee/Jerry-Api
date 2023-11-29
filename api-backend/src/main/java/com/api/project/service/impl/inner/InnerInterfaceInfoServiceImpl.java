package com.api.project.service.impl.inner;

import com.api.project.common.ErrorCode;
import com.api.project.exception.BusinessException;
import com.api.project.service.InterfaceInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.apache.dubbo.config.annotation.DubboService;
import com.api.common.model.entity.InterfaceInfo;
import com.api.common.service.InnerInterfaceInfoService;

import javax.annotation.Resource;

@DubboService
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {

    @Resource
    private InterfaceInfoService interfaceInfoService;
    @Override
    public InterfaceInfo getInterfaceInfo(String url, String method) {
        if(url == null || method == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url", url).eq("method", method);
        InterfaceInfo interfaceInfo = interfaceInfoService.getOne(queryWrapper);
        return interfaceInfo;
    }
}
