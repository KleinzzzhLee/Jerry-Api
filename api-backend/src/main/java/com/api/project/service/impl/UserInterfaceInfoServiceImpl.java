package com.api.project.service.impl;

import com.api.project.common.BaseResponse;
import com.api.project.common.ErrorCode;
import com.api.project.common.ResultUtils;
import com.api.project.exception.BusinessException;
import com.api.project.service.UserInterfaceInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.api.project.mapper.UserInterfaceInfoMapper;
import com.api.common.model.entity.UserInterfaceInfo;
import org.springframework.stereotype.Service;

/**
* @author Lzzh
* @description 针对表【user_interface_info(用户)】的数据库操作Service实现
* @createDate 2023-11-15 16:30:59
*/
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
    implements UserInterfaceInfoService {

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if(userInterfaceInfo.getInterfaceInfoId() == null || userInterfaceInfo.getId()==null
        || userInterfaceInfo.getInterfaceInfoId() <= 0 || userInterfaceInfo.getUserId() <= 0
        ) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if(userInterfaceInfo.getTotalTimes() < 0 || userInterfaceInfo.getLeaveTimes() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }

    @Override
    public boolean invoke(long interfaceInfoId, long userId) {
        // 判断这两是否合法
        if(interfaceInfoId <=0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 联系数据库，查询记录是否存在
        QueryWrapper<UserInterfaceInfo> userInterfaceInfoQueryWrapper = new QueryWrapper<>();
        userInterfaceInfoQueryWrapper.eq("userId", userId);
        userInterfaceInfoQueryWrapper.eq("interfaceInfoId", interfaceInfoId);
        UserInterfaceInfo userInterfaceInfo = this.getOne(userInterfaceInfoQueryWrapper);
        if(userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(userInterfaceInfo.getLeaveTimes() <= 0) {
            throw new BusinessException(-1, "剩余次数不足");
        }
        // 对上述内容进行更新
        userInterfaceInfo.setLeaveTimes(userInterfaceInfo.getLeaveTimes() - 1);
        boolean index = this.update(userInterfaceInfo, userInterfaceInfoQueryWrapper);
        if(!index) {
            throw new BusinessException(-1,"更新异常");
        }
        return true;
    }
}




