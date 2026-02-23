package com.example.back.controller.user;

import com.example.back.common.result.Result;
import com.example.back.dto.CategoryVO;
import com.example.back.service.UserCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserCategoryController {

    private final UserCategoryService userCategoryService;

    /**
     * 获取全类目列表 (树形结构)
     */
    @GetMapping("/category/list")
    public Result<List<CategoryVO>> listCategory() {
        List<CategoryVO> list = userCategoryService.listCategoryTree();
        return Result.success(list);
    }
}
