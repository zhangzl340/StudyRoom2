package com.studyroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("check_ins")
public class CheckIn implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reservationId;

    private Long userId;

    private Date checkInTime;

    private Date checkOutTime;

    private String checkInMethod;

    private String status;

    private Date createdAt;

    private Date updatedAt;
}