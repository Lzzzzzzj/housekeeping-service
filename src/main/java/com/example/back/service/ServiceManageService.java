package com.example.back.service;

import com.example.back.dto.ServiceSaveDTO;
import com.example.back.entity.pms.PmsService;

import java.util.List;

/**
 * 服务类型管理（增删改查）
 */
public interface ServiceManageService {

    void create(ServiceSaveDTO dto);

    void update(Long id, ServiceSaveDTO dto);

    void delete(Long id);

    PmsService getById(Long id);

    /**
     * 分页查询服务列表（管理员 & 用户共用）
     */
    List<PmsService> page(Integer categoryId, String title, Integer page, Integer pageSize);
}

