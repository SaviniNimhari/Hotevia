package com.hotevia.dao;

import com.hotevia.config.DatabaseConnection;
import com.hotevia.model.RoomCategory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomCategoryDAO {

    public List<RoomCategory> findAll() throws SQLException {
        List<RoomCategory> list = new ArrayList<>();
        String sql = "SELECT category_id, category_name, description, price_per_night, capacity, created_at FROM room_categories ORDER BY category_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public RoomCategory findById(int categoryId) throws SQLException {
        String sql = "SELECT category_id, category_name, description, price_per_night, capacity, created_at FROM room_categories WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean insert(RoomCategory category) throws SQLException {
        String sql = "INSERT INTO room_categories (category_name, description, price_per_night, capacity) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getDescription());
            ps.setBigDecimal(3, category.getPricePerNight());
            ps.setInt(4, category.getCapacity());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        category.setCategoryId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(RoomCategory category) throws SQLException {
        String sql = "UPDATE room_categories SET category_name = ?, description = ?, price_per_night = ?, capacity = ? WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getDescription());
            ps.setBigDecimal(3, category.getPricePerNight());
            ps.setInt(4, category.getCapacity());
            ps.setInt(5, category.getCategoryId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int categoryId) throws SQLException {
        String sql = "DELETE FROM room_categories WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;
        }
    }

    private RoomCategory mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new RoomCategory(
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("description"),
                rs.getBigDecimal("price_per_night"),
                rs.getInt("capacity"),
                ts != null ? ts.toLocalDateTime() : null
        );
    }
}
