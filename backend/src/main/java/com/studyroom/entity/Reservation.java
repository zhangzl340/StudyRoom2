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

    private Long userId;    //用户ID

    private Long seatId;//座位ID

    private String status;//状态(正常，违约)

    private String reservationStatus;//预约状态(已预约，使用中，违约中,取消预约，完成预约)

    private Date reservationInTime;//预约进入时间

    private Date reservationOutTime;//预约离开时间

    private Date signInTime;//实际签到时间

    private Date signOutTime;//实际签退时间

    private String remark;//备注

    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
}
