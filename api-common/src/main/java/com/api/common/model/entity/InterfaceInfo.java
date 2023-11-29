package com.api.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
* api.`InterfaceInfo` 接口信息表
* @TableName InterfaceInfo
*/
@Data
@TableName("interface_info")
public class InterfaceInfo implements Serializable {

    /**
    * 主键
    */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
    * 接口名称
    */
    private String name;
    /**
    * 接口描述
    */
    private String description;
    /**
    * 接口地址
    */
    private String url;
    /**
    * 请求类型
    */
    private String method;
    /**
    * 创建人
    */
    private Long userId;

    /**
     * 请求参数
     */
    private String requestParams;
    /**
    * 请求头
    */
    private String requestHeader;
    /**
    * 响应头
    */
    private String responseHeader;
    /**
    * 接口状态 0 关闭
    */
    private Integer status;
    /**
    * 创建时间
    */
    private Date createTime;
    /**
    * 修改时间
    */
    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
