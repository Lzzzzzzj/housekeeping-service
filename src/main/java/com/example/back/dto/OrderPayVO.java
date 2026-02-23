package com.example.back.dto;

import lombok.Data;

/**
 * 微信支付预支付单返回 (占位结构，后续对接微信支付SDK)
 */
@Data
public class OrderPayVO {
    private String orderSn;
    private String prepayId;      // 微信预支付ID
    private String paySign;       // 签名
    private String timeStamp;
    private String nonceStr;
}
