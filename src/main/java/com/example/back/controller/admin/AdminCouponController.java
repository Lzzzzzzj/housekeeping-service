package com.example.back.controller.admin;

import com.example.back.common.result.Result;
import com.example.back.dto.CouponSaveDTO;
import com.example.back.dto.CouponStatusDTO;
import com.example.back.entity.mkt.MktCoupon;
import com.example.back.security.UserContext;
import com.example.back.service.AdminCouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/coupon")
@RequiredArgsConstructor
public class AdminCouponController {

    private static final int USER_TYPE_ADMIN = 3;

    private final AdminCouponService adminCouponService;

    private Long requireAdmin() {
        Long userId = UserContext.getUserId();
        Integer userType = UserContext.getUserType();
        if (userId == null || userType == null || userType != USER_TYPE_ADMIN) {
            throw new IllegalArgumentException("需要管理员权限");
        }
        return userId;
    }

    /**
     * 创建优惠券
     */
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody CouponSaveDTO dto) {
        requireAdmin();
        adminCouponService.create(dto);
        return Result.success();
    }

    /**
     * 更新优惠券
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CouponSaveDTO dto) {
        requireAdmin();
        adminCouponService.update(id, dto);
        return Result.success();
    }

    /**
     * 上架/下架优惠券
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody CouponStatusDTO dto) {
        requireAdmin();
        adminCouponService.updateStatus(id, dto.getStatus());
        return Result.success();
    }

    /**
     * 优惠券分页列表
     */
    @GetMapping("/page")
    public Result<List<MktCoupon>> page(@RequestParam(required = false) Integer status,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false, defaultValue = "1") Integer page,
                                        @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        requireAdmin();
        List<MktCoupon> list = adminCouponService.page(status, name, page, pageSize);
        return Result.success(list);
    }
}

