package com.wenhua.tcpservice.pojo;

/**
 * 用户权限等级，与文档定义一致：
 * <ul>
 *   <li>{@link #OBSERVER} — 观察员 (1)</li>
 *   <li>{@link #OPERATOR} — 运维工程师 (2)</li>
 *   <li>{@link #ADMIN}    — 系统管理员 (3)</li>
 * </ul>
 */
public enum PermissionLevel
{
    OBSERVER(1, "观察员"),
    OPERATOR(2, "运维工程师"),
    ADMIN(3, "系统管理员");

    private final int level;
    private final String displayName;

    PermissionLevel(int level, String displayName)
    {
        this.level = level;
        this.displayName = displayName;
    }

    public int getLevel() { return level; }

    public String getDisplayName() { return displayName; }

    /**
     * 检查当前权限是否满足 {@code required} 门槛（等级值 >= required 的等级值）。
     */
    public boolean hasPermission(PermissionLevel required)
    {
        return this.level >= required.level;
    }

    /**
     * 将数据库中的整数转换为对应的枚举值。
     *
     * @param level 权限等级（1/2/3），非法值默认返回 {@link #OBSERVER}
     */
    public static PermissionLevel fromInt(int level)
    {
        return switch (level)
        {
            case 1 -> OBSERVER;
            case 2 -> OPERATOR;
            case 3 -> ADMIN;
            default -> OBSERVER;
        };
    }
}
