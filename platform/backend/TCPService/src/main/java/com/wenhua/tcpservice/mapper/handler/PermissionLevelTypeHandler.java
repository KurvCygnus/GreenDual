package com.wenhua.tcpservice.mapper.handler;

import com.wenhua.tcpservice.pojo.PermissionLevel;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis {@code int ↔ PermissionLevel} 类型转换器.
 * <p>数据库存储为整数（1/2/3），Java 映射为枚举。</p>
 */
@MappedTypes(PermissionLevel.class)
@MappedJdbcTypes(JdbcType.INTEGER)
public class PermissionLevelTypeHandler extends BaseTypeHandler<PermissionLevel>
{
    @Override public void setNonNullParameter(PreparedStatement ps, int i, PermissionLevel parameter, JdbcType jdbcType) throws SQLException
        { ps.setInt(i, parameter.getLevel()); }

    @Override public PermissionLevel getNullableResult(ResultSet rs, String columnName) throws SQLException
    {
        final var level = rs.getInt(columnName);
        return rs.wasNull() ? null : PermissionLevel.fromInt(level);
    }

    @Override public PermissionLevel getNullableResult(ResultSet rs, int columnIndex) throws SQLException
    {
        final var level = rs.getInt(columnIndex);
        return rs.wasNull() ? null : PermissionLevel.fromInt(level);
    }

    @Override public PermissionLevel getNullableResult(CallableStatement cs, int columnIndex) throws SQLException
    {
        final var level = cs.getInt(columnIndex);
        return cs.wasNull() ? null : PermissionLevel.fromInt(level);
    }
}
