package com.example.back.controller.user;

import com.example.back.common.result.Result;
import com.example.back.dto.StaffApplyDTO;
import com.example.back.dto.StaffApplyVO;
import com.example.back.entity.ums.UmsStaffApply;
import com.example.back.security.UserContext;
import com.example.back.service.StaffApplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端 - 服务人员申请
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserStaffApplyController {

    private final StaffApplyService staffApplyService;

    /**
     * 申请成为服务人员
     */
    @PostMapping("/staff/apply")
    public Result<UmsStaffApply> apply(@Valid @RequestBody StaffApplyDTO dto) {
        Long memberId = requireMemberId();
        UmsStaffApply apply = staffApplyService.apply(memberId, dto);
        return Result.success(apply);
    }

    /**
     * 查询我的申请状态
     */
    @GetMapping("/staff/apply/status")
    public Result<StaffApplyVO> getMyApply() {
        Long memberId = requireMemberId();
        StaffApplyVO vo = staffApplyService.getMyApply(memberId);
        return Result.success(vo);
    }

    private Long requireMemberId() {
        Long memberId = UserContext.getMemberId();
        if (memberId == null) {
            throw new IllegalArgumentException("请登录");
        }
        return memberId;
    }
}
