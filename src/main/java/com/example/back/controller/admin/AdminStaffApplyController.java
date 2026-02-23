package com.example.back.controller.admin;

import com.example.back.common.result.Result;
import com.example.back.dto.StaffApplyAuditDTO;
import com.example.back.dto.StaffApplyVO;
import com.example.back.security.UserContext;
import com.example.back.service.StaffApplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员端 - 服务人员申请审核
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminStaffApplyController {

    private static final int USER_TYPE_ADMIN = 3;

    private final StaffApplyService staffApplyService;

    /**
     * 获取申请列表（支持按状态筛选、分页）
     * @param status 0-待审核, 1-已通过, 2-驳回，不传则查全部
     */
    @GetMapping("/staff/apply/list")
    public Result<List<StaffApplyVO>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        requireAdmin();
        List<StaffApplyVO> list = staffApplyService.listForAdmin(status, page, pageSize);
        return Result.success(list);
    }

    /**
     * 审核申请（通过/驳回）
     */
    @PutMapping("/staff/apply/audit")
    public Result<Void> audit(@Valid @RequestBody StaffApplyAuditDTO dto) {
        Long adminUserId = requireAdmin();
        staffApplyService.audit(adminUserId, dto);
        return Result.success();
    }

    private Long requireAdmin() {
        Long userId = UserContext.getUserId();
        Integer userType = UserContext.getUserType();
        if (userId == null || userType == null || userType != USER_TYPE_ADMIN) {
            throw new IllegalArgumentException("需要管理员权限");
        }
        return userId;
    }
}
