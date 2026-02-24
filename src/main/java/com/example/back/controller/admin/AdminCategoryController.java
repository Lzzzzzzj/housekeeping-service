package com.example.back.controller.admin;

import com.example.back.common.result.Result;
import com.example.back.dto.CategorySaveDTO;
import com.example.back.entity.pms.PmsCategory;
import com.example.back.security.UserContext;
import com.example.back.service.CategoryManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员端 - 服务类目管理（增删改查）
 */
@RestController
@RequestMapping("/api/v1/admin/category")
@RequiredArgsConstructor
public class AdminCategoryController {

    private static final int USER_TYPE_ADMIN = 3;

    private final CategoryManageService categoryManageService;

    private Long requireAdmin() {
        Long userId = UserContext.getUserId();
        Integer userType = UserContext.getUserType();
        if (userId == null || userType == null || userType != USER_TYPE_ADMIN) {
            throw new IllegalArgumentException("需要管理员权限");
        }
        return userId;
    }

    /**
     * 创建类目
     */
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody CategorySaveDTO dto) {
        requireAdmin();
        categoryManageService.create(dto);
        return Result.success();
    }

    /**
     * 修改类目
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @Valid @RequestBody CategorySaveDTO dto) {
        requireAdmin();
        categoryManageService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除类目
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        requireAdmin();
        categoryManageService.delete(id);
        return Result.success();
    }

    /**
     * 获取单个类目详情
     */
    @GetMapping("/{id}")
    public Result<PmsCategory> detail(@PathVariable Integer id) {
        requireAdmin();
        PmsCategory category = categoryManageService.getById(id);
        return Result.success(category);
    }

    /**
     * 分页查询类目列表（按名称模糊查询）
     */
    @GetMapping("/page")
    public Result<List<PmsCategory>> page(
            @RequestParam(required = false) String name,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        requireAdmin();
        List<PmsCategory> list = categoryManageService.page(name, page, pageSize);
        return Result.success(list);
    }
}

