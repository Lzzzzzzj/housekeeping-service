package com.example.back.service.impl;

import com.example.back.common.constant.StaffApplyStatus;
import com.example.back.dto.StaffApplyAuditDTO;
import com.example.back.dto.StaffApplyDTO;
import com.example.back.dto.StaffApplyVO;
import com.example.back.entity.sys.SysUser;
import com.example.back.entity.ums.UmsMember;
import com.example.back.entity.ums.UmsStaff;
import com.example.back.entity.ums.UmsStaffApply;
import com.example.back.mapper.SysUserMapper;
import com.example.back.mapper.UmsMemberMapper;
import com.example.back.mapper.UmsStaffApplyMapper;
import com.example.back.mapper.UmsStaffMapper;
import com.example.back.service.StaffApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffApplyServiceImpl implements StaffApplyService {

    private static final int USER_TYPE_MEMBER = 1;
    private static final int USER_TYPE_STAFF = 2;
    private static final int STAFF_AUDIT_APPROVED = 1;
    private static final int STAFF_WORK_STATUS_REST = 0;

    private final UmsMemberMapper umsMemberMapper;
    private final SysUserMapper sysUserMapper;
    private final UmsStaffApplyMapper umsStaffApplyMapper;
    private final UmsStaffMapper umsStaffMapper;

    @Override
    @Transactional
    public UmsStaffApply apply(Long memberId, StaffApplyDTO dto) {
        UmsMember member = umsMemberMapper.selectById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("用户信息不存在");
        }
        Long userId = member.getUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getUserType() != USER_TYPE_MEMBER) {
            throw new IllegalArgumentException("仅普通用户可申请成为服务人员");
        }

        // 检查是否已是服务人员
        UmsStaff existStaff = umsStaffMapper.selectByUserId(userId);
        if (existStaff != null) {
            throw new IllegalArgumentException("您已是服务人员，无需重复申请");
        }

        // 检查是否有待审核的申请
        UmsStaffApply pending = umsStaffApplyMapper.selectByUserIdAndStatus(userId, StaffApplyStatus.PENDING);
        if (pending != null) {
            throw new IllegalArgumentException("您已有待审核的申请，请耐心等待");
        }

        UmsStaffApply apply = new UmsStaffApply();
        apply.setUserId(userId);
        apply.setRealName(dto.getRealName());
        apply.setIdCard(dto.getIdCard());
        apply.setPhone(dto.getPhone() != null ? dto.getPhone() : user.getPhone());
        apply.setHealthCertUrl(dto.getHealthCertUrl());
        apply.setSkillCertUrls(dto.getSkillCertUrls());
        apply.setApplyReason(dto.getApplyReason());
        apply.setStatus(StaffApplyStatus.PENDING);

        umsStaffApplyMapper.insert(apply);
        return apply;
    }

    @Override
    public StaffApplyVO getMyApply(Long memberId) {
        UmsMember member = umsMemberMapper.selectById(memberId);
        if (member == null) return null;
        Long userId = member.getUserId();

        UmsStaffApply apply = umsStaffApplyMapper.selectLatestByUserId(userId);
        if (apply == null) return null;
        SysUser user = sysUserMapper.selectById(userId);
        return toVO(apply, user);
    }

    @Override
    public List<StaffApplyVO> listForAdmin(Integer status, Integer page, Integer pageSize) {
        int offset = (page != null && page > 0 ? page - 1 : 0) * (pageSize != null && pageSize > 0 ? pageSize : 20);
        int limit = pageSize != null && pageSize > 0 ? pageSize : 20;

        List<UmsStaffApply> list;
        if (status != null) {
            list = umsStaffApplyMapper.selectByStatus(status, offset, limit);
        } else {
            list = umsStaffApplyMapper.selectAll(offset, limit);
        }

        List<StaffApplyVO> result = new ArrayList<>();
        for (UmsStaffApply a : list) {
            SysUser user = sysUserMapper.selectById(a.getUserId());
            result.add(toVO(a, user));
        }
        return result;
    }

    @Override
    @Transactional
    public void audit(Long adminUserId, StaffApplyAuditDTO dto) {
        UmsStaffApply apply = umsStaffApplyMapper.selectById(dto.getApplyId());
        if (apply == null) {
            throw new IllegalArgumentException("申请不存在");
        }
        if (apply.getStatus() != StaffApplyStatus.PENDING) {
            throw new IllegalArgumentException("该申请已处理，无法重复审核");
        }

        if (Boolean.TRUE.equals(dto.getApproved())) {
            // 通过：创建 ums_staff，更新 sys_user.user_type
            SysUser user = sysUserMapper.selectById(apply.getUserId());
            if (user == null) {
                throw new IllegalArgumentException("申请人不存在");
            }
            if (umsStaffMapper.selectByUserId(apply.getUserId()) != null) {
                throw new IllegalArgumentException("该用户已是服务人员");
            }

            UmsStaff staff = new UmsStaff();
            staff.setUserId(apply.getUserId());
            staff.setRealName(apply.getRealName());
            staff.setIdCard(apply.getIdCard());
            staff.setHealthCertUrl(apply.getHealthCertUrl());
            staff.setServiceScore(BigDecimal.valueOf(5.0));
            staff.setOrderCount(0);
            staff.setWorkStatus(STAFF_WORK_STATUS_REST);
            staff.setAuditStatus(STAFF_AUDIT_APPROVED);
            staff.setAutoAccept(0);
            staff.setBalance(BigDecimal.ZERO);
            umsStaffMapper.insert(staff);

            sysUserMapper.updateUserType(apply.getUserId(), USER_TYPE_STAFF);
            umsStaffApplyMapper.updateStatus(dto.getApplyId(), StaffApplyStatus.APPROVED, null, adminUserId);
        } else {
            // 驳回
            if (dto.getRejectReason() == null || dto.getRejectReason().isBlank()) {
                throw new IllegalArgumentException("驳回时请填写驳回原因");
            }
            umsStaffApplyMapper.updateStatus(dto.getApplyId(), StaffApplyStatus.REJECTED, dto.getRejectReason(), adminUserId);
        }
    }

    private StaffApplyVO toVO(UmsStaffApply apply, SysUser user) {
        StaffApplyVO vo = new StaffApplyVO();
        BeanUtils.copyProperties(apply, vo);
        if (user != null) {
            vo.setApplicantNickname(user.getNickname());
            vo.setApplicantUsername(user.getUsername());
        }
        return vo;
    }
}
