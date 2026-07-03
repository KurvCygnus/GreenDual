package com.wenhua.tcpservice.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Integer userId;
    private String username;
    private String password;
    private Integer userPermission;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;
}
