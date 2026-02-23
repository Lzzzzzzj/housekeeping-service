package com.example.back.service;

import com.example.back.dto.StaffApplyAuditDTO;
import com.example.back.dto.StaffApplyDTO;
import com.example.back.dto.StaffApplyVO;
import com.example.back.entity.ums.UmsStaffApply;

import java.util.List;

/**
 * 服务人员申请服务
 */
public interface StaffApplyService {

    /**
     * 用户申请成为服务人员
     */
    UmsStaffApply apply(Long memberId, StaffApplyDTO dto);

    /**
     * 用户查询自己的申请状态
     */
    StaffApplyVO getMyApply(Long memberId);

    /**
     * 管理员：获取申请列表（支持按状态筛选）
     */
    List<StaffApplyVO> listForAdmin(Integer status, Integer page, Integer pageSize);

    /**
     * 管理员：审核申请（通过/驳回）
     */
    void audit(Long adminUserId, StaffApplyAuditDTO dto);
}
