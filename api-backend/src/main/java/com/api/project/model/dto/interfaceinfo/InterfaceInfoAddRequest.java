package com.api.project.model.dto.interfaceinfo;

import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 创建请求
 *  Dto data transfer object 就是在进行数据传递封装信息的 对象
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class    InterfaceInfoAddRequest implements Serializable {



    /**
     * 接口名称
     */
    @NotBlank(message="[接口名称]不能为空")
    @Size(max= 256,message="编码长度不能超过256")
    @ApiModelProperty("接口名称")
    private String name;
    /**
     * 接口描述
     */
    @Size(max= 256,message="编码长度不能超过256")
    @ApiModelProperty("接口描述")
    private String description;
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
     * 创建人  后台自动同步
     */
//    @NotNull(message="[创建人]不能为空")
//    @ApiModelProperty("创建人")
//    private Long userId;
    /**
     * 请求头
     */
    @Size(max= -1,message="编码长度不能超过-1")
    @ApiModelProperty("请求头")
    private String requestHeader;
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
    private String requestParams;
    /**
     * 响应头
     */
    @Size(max= -1,message="编码长度不能超过-1")
    @ApiModelProperty("响应头")
    private String responseHeader;
    /**
     * 接口状态 0 关闭 接口状态默认关闭
     */
//    @NotNull(message="[接口状态 0 关闭]不能为空")
//    @ApiModelProperty("接口状态 0 关闭")
//    private Integer status;
}
