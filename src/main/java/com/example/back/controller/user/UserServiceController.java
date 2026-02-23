package com.example.back.controller.user;

import com.example.back.common.result.Result;
import com.example.back.entity.pms.PmsService;
import com.example.back.service.ServiceManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端 - 服务类型查询（仅查看）
 */
@RestController
@RequestMapping("/api/v1/user/service")
@RequiredArgsConstructor
public class UserServiceController {

    private final ServiceManageService serviceManageService;

    /**
     * 分页查询服务列表（用户只能查看）
     */
    @GetMapping("/page")
    public Result<List<PmsService>> page(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        List<PmsService> list = serviceManageService.page(categoryId, title, page, pageSize);
        return Result.success(list);
    }
}

