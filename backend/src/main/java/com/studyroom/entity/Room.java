package com.studyroom.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("rooms")
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String image;

    private String location;

    private Integer rowsCount;

    private Integer colsCount;

    private String openTime;

    private String closeTime;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
}
