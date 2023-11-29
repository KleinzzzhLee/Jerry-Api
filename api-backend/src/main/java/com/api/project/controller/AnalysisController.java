package com.api.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.api.project.annotation.AuthCheck;
import com.api.project.common.BaseResponse;
import com.api.project.common.ErrorCode;
import com.api.project.common.ResultUtils;
import com.api.project.exception.BusinessException;
import com.api.project.mapper.UserInterfaceInfoMapper;
import com.api.project.model.vo.InterfaceInfoVO;
import com.api.project.service.InterfaceInfoService;
import com.api.common.model.entity.InterfaceInfo;
import com.api.common.model.entity.UserInterfaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分析控制器
 *
 */
@RestController
@RequestMapping("/analysis")
@Slf4j
public class AnalysisController {

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @GetMapping("/top/interface/invoke")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<InterfaceInfoVO>> listTopInvokeInterfaceInfo() {
        List<InterfaceInfoVO> interfaceInfoVOList = userInterfaceInfoMapper.listTopInvokeInterfaceInfo(3);
        List<Long> ids = interfaceInfoVOList.stream().map(InterfaceInfo::getId).collect(Collectors.toList());

        Map<Long, InterfaceInfoVO> interfaceInfoVOMap = new HashMap<>();
        for(InterfaceInfoVO interfaceInfoVO : interfaceInfoVOList) {
            interfaceInfoVOMap.put(interfaceInfoVO.getId(), interfaceInfoVO);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list(queryWrapper);

        for(InterfaceInfo interfaceInfo : interfaceInfoList) {
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVOMap.get(interfaceInfo.getId()));
        }


        return ResultUtils.success(interfaceInfoVOList);
    }
}
