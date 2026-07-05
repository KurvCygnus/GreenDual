package com.wenhua.tcpservice.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Objects;
import java.util.Optional;

@ToString
public final class QueryParameter
{
    // ========== 查询类型枚举 ==========
    public enum QueryType
    {
        ALL(0), ONLINE(1), OFFLINE(2);
        
        public final int value;
        
        QueryType(int value) { this.value = value; }
        
        public static QueryType fromValue(int value)
        {
            for(QueryType t: values())
                if(t.value == value)
                    return t;
            return ALL;
        }
    }
    
    // ========== 排序常量（供 MyBatis XML 引用） ==========
    public static final String DESC = "desc";
    public static final String ASC = "asc";
    
    // ========== 字段 ==========
    @Getter private final int currentPage;
    @Getter private final int pageSize;
    @Getter private final int offset;           // (currentPage-1) * pageSize
    @Getter private final String sort;             // null → 不排序
    @Getter private final String match;            // null → 不模糊匹配
    @Getter private final String username;         // null → 不按用户名筛选
    @Getter private final QueryType query;
    private final @Nullable Integer permission; // null → 不按权限筛选
    
    // ========== 构造 ==========
    
    /**
     * JSON 反序列化入口，所有装箱参数均提供空安全默认值。
     */
    @JsonCreator
    public QueryParameter(
        @JsonProperty("currentPage") Integer currentPage,
        @JsonProperty("pageSize") Integer pageSize,
        @JsonProperty("sort") String sort,
        @JsonProperty("match") String match,
        @JsonProperty("username") String username,
        @JsonProperty("query") QueryType query,
        @JsonProperty("permission") @Nullable Integer permission
    )
    {
        this.currentPage = Objects.requireNonNullElse(currentPage, 1);
        this.pageSize = Objects.requireNonNullElse(pageSize, 10);
        this.offset = (this.currentPage - 1) * this.pageSize;
        this.sort = sort;
        this.match = match;
        this.username = username;
        this.query = query != null ? query : QueryType.ALL;
        this.permission = permission;
    }
    
    // ========== 只读 getter ==========
    public @NonNull Optional<Integer> getPermission() { return Optional.ofNullable(permission); }
    
    @Override public boolean equals(Object o)
    {
        if(this == o) return true;
        if(!(o instanceof QueryParameter that)) return false;
        return currentPage == that.currentPage
            && pageSize == that.pageSize
            && Objects.equals(sort, that.sort)
            && Objects.equals(match, that.match)
            && Objects.equals(username, that.username)
            && query == that.query
            && Objects.equals(permission, that.permission);
    }
    
    @Override public int hashCode() { return Objects.hash(currentPage, pageSize, sort, match, username, query, permission); }
}