package com.api.project.mapper;

import com.api.project.model.vo.InterfaceInfoVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.api.common.model.entity.UserInterfaceInfo;

import java.util.List;

/**
* @author Lzzh
* @description 针对表【user_interface_info(用户)】的数据库操作Mapper
* @createDate 2023-11-15 16:30:59
* @Entity com.api.project.model.entity.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {
    List<InterfaceInfoVO> listTopInvokeInterfaceInfo(int limit);
}




