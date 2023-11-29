package com.api.project.common;

/**
 * 错误码
 *
 * @author yupi
 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    ACCOUNT_NOTEXIST(30000, "账户不存在"),
    REDIS_DELET_EERROR(35001, "redis删除键出现问题"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),
    FILE_CREATE_ERROR(80001, "文件创建失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
