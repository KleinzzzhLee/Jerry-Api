package com.api.project.service;

import com.api.project.common.BaseResponse;
import com.baomidou.mybatisplus.extension.service.IService;
import com.api.common.model.entity.UserInterfaceInfo;

/**
* @author Lzzh
* @description 针对表【user_interface_info(用户)】的数据库操作Service
* @createDate 2023-11-15 16:30:59
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {
    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);

    boolean invoke(long interfaceInfoId, long userId);
}
