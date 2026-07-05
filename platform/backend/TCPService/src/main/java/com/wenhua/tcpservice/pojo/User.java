package com.wenhua.tcpservice.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * 用户实体。
 * <p>为兼容 MyBatis 自动映射与自增主键回写，userId 在持久化前为 0。</p>
 */
@ToString
public final class User
{
    /**
     * setter 回写自增主键（仅 MyBatis 框架调用）
     */
    @Getter @Setter private int userId;

    @Getter private final String username;

    @Getter @Setter private String password;

    private PermissionLevel userPermission;

    @Getter private final LocalDateTime createTime;

    /**
     * 回写登录时间（仅 LoginController 调用）
     */
    @Getter @Setter private LocalDateTime lastLoginTime;

    /**
     * JSON 反序列化 + 手动构造
     */
    @JsonCreator
    public User(
        @JsonProperty("userId") Integer userId,
        @JsonProperty("username") String username,
        @JsonProperty("password") String password,
        @JsonProperty("userPermission") Integer userPermission,
        @JsonProperty("createTime") LocalDateTime createTime,
        @JsonProperty("lastLoginTime") LocalDateTime lastLoginTime
    )
    {
        this.userId = Objects.requireNonNullElse(userId, 0);
        this.username = username;
        this.password = password;
        this.userPermission = PermissionLevel.fromInt(Objects.requireNonNullElse(userPermission, 1));
        this.createTime = createTime;
        this.lastLoginTime = lastLoginTime;
    }

    /**
     * MyBatis 反序列化用（框架通过反射设值）
     */
    User()
    {
        this.userId = 0;
        this.username = null;
        this.password = null;
        this.userPermission = PermissionLevel.OBSERVER;
        this.createTime = null;
        this.lastLoginTime = null;
    }

    /**
     * 向后兼容的权限等级 Getter — Jackson 序列化为 int（如 3），
     * 为 MyBatis 保留的包级私有 setter 设值。
     */
    @JsonProperty("userPermission")
    public int getUserPermission() { return userPermission.getLevel(); }

    /** 获取权限等级枚举。 */
    public PermissionLevel getPermissionLevel() { return userPermission; }

    /**
     * MyBatis 通过 TypeHandler 注入 PermissionLevel 时使用。
     */
    @SuppressWarnings("unused") void setUserPermission(PermissionLevel permissionLevel) { this.userPermission = permissionLevel; }
    
    public @NonNull Optional<LocalDateTime> getLastLoginTime() { return Optional.ofNullable(lastLoginTime); }
    
    public @NonNull Optional<LocalDateTime> getCreateTime() { return Optional.ofNullable(createTime); }
    
    @Override public boolean equals(Object o)
    {
        if(this == o) return true;
        if(!(o instanceof User user)) return false;
        return userId == user.userId;
    }
    
    @Override public int hashCode() { return Objects.hash(userId); }
}