package com.example.back.mapper;

import com.example.back.entity.ums.UmsStaffApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UmsStaffApplyMapper {

    int insert(UmsStaffApply apply);

    UmsStaffApply selectById(@Param("id") Long id);

    UmsStaffApply selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    UmsStaffApply selectLatestByUserId(@Param("userId") Long userId);

    List<UmsStaffApply> selectByStatus(@Param("status") Integer status, @Param("offset") Integer offset, @Param("limit") Integer limit);

    List<UmsStaffApply> selectAll(@Param("offset") Integer offset, @Param("limit") Integer limit);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status,
                     @Param("rejectReason") String rejectReason, @Param("auditUserId") Long auditUserId);

    int countByStatus(@Param("status") Integer status);
}
