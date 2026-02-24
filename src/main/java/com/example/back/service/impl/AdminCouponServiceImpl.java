package com.example.back.service.impl;

import com.example.back.dto.CouponSaveDTO;
import com.example.back.entity.mkt.MktCoupon;
import com.example.back.mapper.MktCouponMapper;
import com.example.back.service.AdminCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCouponServiceImpl implements AdminCouponService {

    private final MktCouponMapper mktCouponMapper;

    @Override
    @Transactional
    public void create(CouponSaveDTO dto) {
        MktCoupon coupon = new MktCoupon();
        BeanUtils.copyProperties(dto, coupon);
        if (coupon.getStatus() == null) {
            coupon.setStatus(1);
        }
        mktCouponMapper.insert(coupon);
    }

    @Override
    @Transactional
    public void update(Long id, CouponSaveDTO dto) {
        MktCoupon exist = mktCouponMapper.selectById(id);
        if (exist == null) {
            throw new IllegalArgumentException("优惠券不存在");
        }
        BeanUtils.copyProperties(dto, exist);
        exist.setId(id);
        mktCouponMapper.update(exist);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        MktCoupon exist = mktCouponMapper.selectById(id);
        if (exist == null) {
            throw new IllegalArgumentException("优惠券不存在");
        }
        exist.setStatus(status);
        mktCouponMapper.update(exist);
    }

    @Override
    public List<MktCoupon> page(Integer status, String name, Integer page, Integer pageSize) {
        int pageNo = (page != null && page > 0) ? page : 1;
        int size = (pageSize != null && pageSize > 0) ? pageSize : 20;
        int offset = (pageNo - 1) * size;
        return mktCouponMapper.pageQuery(status, name, offset, size);
    }
}

