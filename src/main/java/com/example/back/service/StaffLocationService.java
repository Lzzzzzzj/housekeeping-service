package com.example.back.service;

/**
 * 师傅位置相关服务（Redis GEO）。
 */
public interface StaffLocationService {

    /**
     * 上报当前师傅实时经纬度，并写入 Redis GEO。
     *
     * @param staffUserId 当前登录师傅对应的 sys_user.id
     * @param lng         经度
     * @param lat         纬度
     */
    void updateLocation(Long staffUserId, double lng, double lat);
}

