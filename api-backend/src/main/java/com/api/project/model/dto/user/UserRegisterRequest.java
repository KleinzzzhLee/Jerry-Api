package com.api.project.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author yupi
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String userAccount;

    /**
     * 验证码
     */
    private String code;

    private String userPassword;

    private String checkPassword;

    /**
     *  判断是更新密码还是注册
     *  0 为注册
     *  1 为修稿密码
     */
    private Integer update;
}
