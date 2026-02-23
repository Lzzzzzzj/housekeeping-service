package com.example.back.controller.admin;

import com.example.back.common.result.Result;
import com.example.back.dto.ServiceSaveDTO;
import com.example.back.entity.pms.PmsService;
import com.example.back.security.UserContext;
import com.example.back.service.ServiceManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员端 - 服务类型管理（增删改查）
 */
@RestController
@RequestMapping("/api/v1/admin/service")
@RequiredArgsConstructor
public class AdminServiceController {

    private static final int USER_TYPE_ADMIN = 3;

    private final ServiceManageService serviceManageService;

    private Long requireAdmin() {
        Long userId = UserContext.getUserId();
        Integer userType = UserContext.getUserType();
        if (userId == null || userType == null || userType != USER_TYPE_ADMIN) {
            throw new IllegalArgumentException("需要管理员权限");
        }
        return userId;
    }

    /**
     * 创建服务类型
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ServiceSaveDTO dto) {
        requireAdmin();
        serviceManageService.create(dto);
        return Result.success();
    }

    /**
     * 修改服务类型
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ServiceSaveDTO dto) {
        requireAdmin();
        serviceManageService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除服务类型
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        requireAdmin();
        serviceManageService.delete(id);
        return Result.success();
    }

    /**
     * 获取单个服务详情
     */
    @GetMapping("/{id}")
    public Result<PmsService> detail(@PathVariable Long id) {
        requireAdmin();
        PmsService service = serviceManageService.getById(id);
        return Result.success(service);
    }

    /**
     * 分页查询服务列表
     */
    @GetMapping("/page")
    public Result<List<PmsService>> page(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        requireAdmin();
        List<PmsService> list = serviceManageService.page(categoryId, title, page, pageSize);
        return Result.success(list);
    }
}

