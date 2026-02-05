package com.studyroom.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("reservations")
public class Reservation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long seatId;

    private String status;

    private String reservationStatus;

    private Date reservationInTime;

    private Date reservationOutTime;

    private Date signInTime;

    private Date signOutTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
}
