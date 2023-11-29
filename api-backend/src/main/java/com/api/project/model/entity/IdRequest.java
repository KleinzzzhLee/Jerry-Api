package com.api.project.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 帖子
 *
 * @TableName post
 */
@TableName(value = "post")
@Data
public class IdRequest implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
