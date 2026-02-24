package com.example.back.dto;

import lombok.Data;

/**
 * 管理员登录返回信息
 */
@Data
public class AdminLoginVO {

    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private String phone;
    private String avatar;
    private Integer userType;

    /**
     * 管理员扩展信息（来自 sys_admin）
     */
    private SysAdminInfo sysAdmin;

    @Data
    public static class SysAdminInfo {
        private Long id;
        private Long userId;
        private Integer adminType;
        private java.math.BigDecimal balance;
        private Long createdBy;
    }
}

