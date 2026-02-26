package com.example.back.service.impl;

import com.example.back.entity.ums.UmsStaff;
import com.example.back.mapper.UmsStaffMapper;
import com.example.back.service.StaffLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 师傅实时位置上报实现，使用 Redis GEO 存储。
 */
@Service
@RequiredArgsConstructor
public class StaffLocationServiceImpl implements StaffLocationService {

    private static final String GEO_KEY_STAFF_LOCATIONS = "staff:locations";
    private static final String STAFF_ACTIVE_KEY_PREFIX = "staff:last_active:";
    private static final Duration ACTIVE_TTL = Duration.ofMinutes(10);

    private final UmsStaffMapper umsStaffMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void updateLocation(Long staffUserId, double lng, double lat) {
        if (staffUserId == null) {
            throw new IllegalArgumentException("请先登录");
        }
        UmsStaff staff = umsStaffMapper.selectByUserId(staffUserId);
        if (staff == null) {
            throw new IllegalArgumentException("当前账号尚未注册为服务人员");
        }
        if (staff.getAuditStatus() == null || staff.getAuditStatus() != 1) {
            throw new IllegalArgumentException("服务人员尚未审核通过，不能上报位置");
        }

        String member = staff.getId().toString();
        stringRedisTemplate.opsForGeo()
                .add(GEO_KEY_STAFF_LOCATIONS, new Point(lng, lat), member);

        // 记录最近一次上报时间，结合 TTL 粗略判断在线状态
        String lastActiveKey = STAFF_ACTIVE_KEY_PREFIX + member;
        stringRedisTemplate.opsForValue()
                .set(lastActiveKey, String.valueOf(System.currentTimeMillis()), ACTIVE_TTL);
    }
}

