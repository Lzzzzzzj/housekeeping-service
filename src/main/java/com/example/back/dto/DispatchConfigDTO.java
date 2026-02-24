package com.example.back.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 自动派单权重配置 DTO：后台可修改各项占比
 */
@Data
public class DispatchConfigDTO {

    /** 是否启用自动派单 */
    private Boolean enableAuto;

    /** 评分权重 w_s，建议 0~1 */
    @DecimalMin("0") @DecimalMax("1")
    private BigDecimal weightScore;

    /** 距离权重 w_d */
    @DecimalMin("0") @DecimalMax("1")
    private BigDecimal weightDistance;

    /** 准时率/履约权重 w_p */
    @DecimalMin("0") @DecimalMax("1")
    private BigDecimal weightPunctual;

    /** 派单搜索半径（公里），预留 */
    @DecimalMin("0")
    private BigDecimal radiusKm;

    /** 单师傅最大进行中订单数 */
    private Integer maxConcurrentOrders;
}
