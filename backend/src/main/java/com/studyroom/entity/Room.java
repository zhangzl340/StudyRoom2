package com.studyroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

    private String code;

    private String building;

    private Integer floor;

    private Integer capacity;

    private String status;

    private String openTime;

    private String closeTime;

    private String description;

    private Date createdAt;

    private Date updatedAt;
}