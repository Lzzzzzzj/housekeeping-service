package com.example.back.service;

import com.example.back.dto.CouponSaveDTO;
import com.example.back.entity.mkt.MktCoupon;

import java.util.List;

public interface AdminCouponService {

    void create(CouponSaveDTO dto);

    void update(Long id, CouponSaveDTO dto);

    void updateStatus(Long id, Integer status);

    List<MktCoupon> page(Integer status, String name, Integer page, Integer pageSize);
}

