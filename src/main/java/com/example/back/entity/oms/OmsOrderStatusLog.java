package com.example.back.entity.oms;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OmsOrderStatusLog {
    private Long id;
    private Long orderId;
    private Integer preStatus;
    private Integer postStatus;
    private String operator;
    private String remark;
    private LocalDateTime createTime;
}
