package com.api.project.model.dto.interfaceinfo;

import com.api.project.common.PageRequest;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 查询请求
 *  不能直接将 一个entity对象信息cv过来，
 *  这是dto对象， 要看 数据传输过程中需要什么，在传递什么
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class InterfaceInfoQueryRequest extends PageRequest implements Serializable {
    @NotNull(message="[主键]不能为空")
    @ApiModelProperty("主键")
    private Long id;
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
     * 创建人
     */
    @NotNull(message="[创建人]不能为空")
    @ApiModelProperty("创建人")
    private Long userId;
    /**
     * 请求头
     */
    @Size(max= -1,message="编码长度不能超过-1")
    @ApiModelProperty("请求头")
    private String requestHeader;
    /**
     * 响应头
     */
    @Size(max= -1,message="编码长度不能超过-1")
    @ApiModelProperty("响应头")
    private String responseHeader;
    /**
     * 接口状态 0 关闭
     */
    @NotNull(message="[接口状态 0 关闭]不能为空")
    @ApiModelProperty("接口状态 0 关闭")
    private Integer status;

}
