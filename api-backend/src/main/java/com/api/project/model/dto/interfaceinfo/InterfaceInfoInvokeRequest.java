package com.api.project.model.dto.interfaceinfo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class InterfaceInfoInvokeRequest {
    /**
     *  接口的id ，
     */
    private Long id;
    /**
     * 接口地址
     */
    @NotBlank(message="[接口地址]不能为空")
    @Size(max= 512,message="编码长度不能超过512")
    @ApiModelProperty("接口地址")
    private String url;
    /**
     * 请求类型
     */
    @NotBlank(message="[请求类型]不能为空")
    @Size(max= 256,message="编码长度不能超过256")
    @ApiModelProperty("请求类型")
    private String method;

    /**
     * 请求头
     */
    @Size(max= -1,message="编码长度不能超过-1")
    @ApiModelProperty("请求头")
    private String userRequestHeader;
    /**
     * 请求参数
     *  请求参数的说明，对于请求参数，传递的时候一定是JSON的数据格式，可以在后端根据 指定的类型进行转换。
     *  如：
     *      [
     *          {"name" : "paramName", "type" : "paramType"}
     *      ]
     */
    @Size(max= -1,message="编码长度不能超过-1")
    @ApiModelProperty("请求参数")
    private String userRequestParams;

}
