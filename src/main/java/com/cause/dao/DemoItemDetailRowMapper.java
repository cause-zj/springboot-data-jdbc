package com.cause.dao;

import com.cause.domain.DemoItemDetail;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class DemoItemDetailRowMapper implements RowMapper<DemoItemDetail> {

    @Override
    public DemoItemDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
        DemoItemDetail detail = new DemoItemDetail();
        detail.setId(readLong(rs, "id"));
        detail.setName(rs.getString("name"));
        detail.setCategoryId(readLong(rs, "category_id"));
        detail.setCategoryName(rs.getString("category_name"));
        detail.setAmount(rs.getBigDecimal("amount"));
        detail.setRemark(rs.getString("remark"));
        detail.setCreateTime(rs.getObject("create_time", LocalDateTime.class));
        detail.setUpdateTime(rs.getObject("update_time", LocalDateTime.class));
        return detail;
    }

    private static Long readLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}
