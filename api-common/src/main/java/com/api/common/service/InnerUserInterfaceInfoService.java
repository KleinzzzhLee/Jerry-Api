package com.api.common.service;



/**
* @author zzzhlee
* @description 针对表【user_interface_info(用户)】的数据库操作Service
* @createDate 2023-11-15 16:30:59
*/
public interface InnerUserInterfaceInfoService {

    boolean invokeCount(long interfaceInfoId, long userId);
}
