package com.example.back.entity.oms;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 现场加价/增项单
 */
@Data
public class OmsOrderExtra {
    private Long id;
    private Long orderId;
    private String title;
    private BigDecimal amount;
    /**
     * 0-未付, 1-已付
     */
    private Integer payStatus;
    /**
     * 现场图片凭证(JSON数组字符串)
     */
    private String evidencePics;
}

