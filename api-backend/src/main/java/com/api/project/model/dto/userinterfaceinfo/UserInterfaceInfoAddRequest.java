package com.api.project.model.dto.userinterfaceinfo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 创建请求
 *  Dto data transfer object 就是在进行数据传递封装信息的 对象
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class UserInterfaceInfoAddRequest implements Serializable {



    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 调用者id
     */
    private Long userId;

    /**
     * 接口id
     */
    private Long interfaceInfoId;

    /**
     * 总调用次数
     */
    private Integer totalTimes;

    /**
     * 剩余调用次数
     */
    private Integer leaveTimes;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
