package com.hotevia.dao;

import com.hotevia.config.DatabaseConnection;
import com.hotevia.model.Room;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public List<Room> findAll() throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT r.room_id, r.room_number, r.category_id, r.floor_number, r.status, r.created_at, " +
                     "c.category_name, c.price_per_night " +
                     "FROM rooms r " +
                     "LEFT JOIN room_categories c ON r.category_id = c.category_id " +
                     "ORDER BY r.room_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Room findById(int roomId) throws SQLException {
        String sql = "SELECT r.room_id, r.room_number, r.category_id, r.floor_number, r.status, r.created_at, " +
                     "c.category_name, c.price_per_night " +
                     "FROM rooms r " +
                     "LEFT JOIN room_categories c ON r.category_id = c.category_id " +
                     "WHERE r.room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds rooms available for the given date range.
     * Checks that room is not under maintenance AND has no overlapping active reservations.
     * Overlap condition: (res.check_in_date < targetCheckOut) AND (res.check_out_date > targetCheckIn)
     */
    public List<Room> findAvailableRoomsForDates(LocalDate checkIn, LocalDate checkOut, Integer excludeReservationId) throws SQLException {
        List<Room> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT r.room_id, r.room_number, r.category_id, r.floor_number, r.status, r.created_at, " +
            "c.category_name, c.price_per_night " +
            "FROM rooms r " +
            "JOIN room_categories c ON r.category_id = c.category_id " +
            "WHERE r.status != 'MAINTENANCE' " +
            "AND r.room_id NOT IN (" +
            "    SELECT res.room_id FROM reservations res " +
            "    WHERE res.status IN ('CONFIRMED', 'CHECKED_IN') " +
            "    AND (res.check_in_date < ? AND res.check_out_date > ?) "
        );

        if (excludeReservationId != null) {
            sql.append("    AND res.reservation_id != ? ");
        }
        sql.append(") ORDER BY r.room_number ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setDate(1, Date.valueOf(checkOut));
            ps.setDate(2, Date.valueOf(checkIn));
            if (excludeReservationId != null) {
                ps.setInt(3, excludeReservationId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public boolean insert(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (room_number, category_id, floor_number, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, room.getRoomNumber());
            ps.setInt(2, room.getCategoryId());
            ps.setInt(3, room.getFloorNumber());
            ps.setString(4, room.getStatus() != null ? room.getStatus() : "AVAILABLE");
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        room.setRoomId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Room room) throws SQLException {
        String sql = "UPDATE rooms SET room_number = ?, category_id = ?, floor_number = ?, status = ? WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getRoomNumber());
            ps.setInt(2, room.getCategoryId());
            ps.setInt(3, room.getFloorNumber());
            ps.setString(4, room.getStatus());
            ps.setInt(5, room.getRoomId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int roomId, String status) throws SQLException {
        String sql = "UPDATE rooms SET status = ? WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int roomId) throws SQLException {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    public int countTotalRooms() throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countRoomsByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        Room room = new Room(
                rs.getInt("room_id"),
                rs.getString("room_number"),
                rs.getInt("category_id"),
                rs.getInt("floor_number"),
                rs.getString("status"),
                ts != null ? ts.toLocalDateTime() : null
        );
        room.setCategoryName(rs.getString("category_name"));
        room.setPricePerNight(rs.getBigDecimal("price_per_night"));
        return room;
    }
}
