package com.example.back.service.impl;

import com.example.back.dto.ServiceSaveDTO;
import com.example.back.entity.pms.PmsService;
import com.example.back.mapper.PmsServiceMapper;
import com.example.back.service.ServiceManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceManageServiceImpl implements ServiceManageService {

    private final PmsServiceMapper pmsServiceMapper;

    @Override
    @Transactional
    public void create(ServiceSaveDTO dto) {
        PmsService service = new PmsService();
        BeanUtils.copyProperties(dto, service);
        if (service.getAllowCoupon() == null) {
            service.setAllowCoupon(1);
        }
        pmsServiceMapper.insert(service);
    }

    @Override
    @Transactional
    public void update(Long id, ServiceSaveDTO dto) {
        PmsService exist = pmsServiceMapper.selectById(id);
        if (exist == null) {
            throw new IllegalArgumentException("服务不存在");
        }
        BeanUtils.copyProperties(dto, exist);
        exist.setId(id);
        if (exist.getAllowCoupon() == null) {
            exist.setAllowCoupon(1);
        }
        pmsServiceMapper.update(exist);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PmsService exist = pmsServiceMapper.selectById(id);
        if (exist == null) {
            throw new IllegalArgumentException("服务不存在");
        }
        pmsServiceMapper.deleteById(id);
    }

    @Override
    public PmsService getById(Long id) {
        return pmsServiceMapper.selectById(id);
    }

    @Override
    public List<PmsService> page(Integer categoryId, String title, Integer page, Integer pageSize) {
        int pageNo = (page != null && page > 0) ? page : 1;
        int size = (pageSize != null && pageSize > 0) ? pageSize : 20;
        int offset = (pageNo - 1) * size;
        return pmsServiceMapper.pageQuery(categoryId, title, offset, size);
    }
}

