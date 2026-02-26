package com.example.back.controller.staff;

import com.example.back.common.result.Result;
import com.example.back.dto.StaffLocationUpdateDTO;
import com.example.back.security.UserContext;
import com.example.back.service.StaffLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 师傅端 - 实时位置上报接口
 *
 * 前缀：/api/v1/staff
 * 路径：/location/update
 */
@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffLocationController {

    private static final int USER_TYPE_STAFF = 2;

    private final StaffLocationService staffLocationService;

    /**
     * 师傅上报当前经纬度，写入 Redis GEO。
     */
    @PostMapping("/location/update")
    public Result<Void> updateLocation(@Valid @RequestBody StaffLocationUpdateDTO dto) {
        Long staffUserId = requireStaffUserId();
        staffLocationService.updateLocation(staffUserId, dto.getLng(), dto.getLat());
        return Result.success();
    }

    private Long requireStaffUserId() {
        Long userId = UserContext.getUserId();
        Integer userType = UserContext.getUserType();
        if (userId == null || userType == null || userType != USER_TYPE_STAFF) {
            throw new IllegalArgumentException("需要服务人员权限");
        }
        return userId;
    }
}

