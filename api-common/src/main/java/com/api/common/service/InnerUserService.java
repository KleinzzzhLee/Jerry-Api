package com.api.common.service;


import com.api.common.model.entity.User;


/**
 * 用户服务
 *
 * @author zzzhlee
 */
public interface InnerUserService {

    User getInvokeUser(String appKey);
}
