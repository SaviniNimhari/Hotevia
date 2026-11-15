package com.hotevia.dao;

import com.hotevia.config.DatabaseConnection;
import com.hotevia.model.CheckInOut;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CheckInOutDAO {

    public List<CheckInOut> findAll() throws SQLException {
        List<CheckInOut> list = new ArrayList<>();
        String sql = "SELECT cio.record_id, cio.reservation_id, cio.actual_check_in, cio.actual_check_out, cio.notes, cio.processed_by, " +
                     "r.reservation_code, c.name AS customer_name, rm.room_number, u.full_name AS processed_by_name " +
                     "FROM check_in_out cio " +
                     "JOIN reservations r ON cio.reservation_id = r.reservation_id " +
                     "JOIN customers c ON r.customer_id = c.customer_id " +
                     "JOIN rooms rm ON r.room_id = rm.room_id " +
                     "LEFT JOIN users u ON cio.processed_by = u.user_id " +
                     "ORDER BY cio.record_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public CheckInOut findByReservationId(int reservationId) throws SQLException {
        String sql = "SELECT cio.record_id, cio.reservation_id, cio.actual_check_in, cio.actual_check_out, cio.notes, cio.processed_by, " +
                     "r.reservation_code, c.name AS customer_name, rm.room_number, u.full_name AS processed_by_name " +
                     "FROM check_in_out cio " +
                     "JOIN reservations r ON cio.reservation_id = r.reservation_id " +
                     "JOIN customers c ON r.customer_id = c.customer_id " +
                     "JOIN rooms rm ON r.room_id = rm.room_id " +
                     "LEFT JOIN users u ON cio.processed_by = u.user_id " +
                     "WHERE cio.reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean recordCheckIn(int reservationId, String notes, Integer processedBy) throws SQLException {
        String sql = "INSERT INTO check_in_out (reservation_id, actual_check_in, notes, processed_by) VALUES (?, NOW(), ?, ?) " +
                     "ON DUPLICATE KEY UPDATE actual_check_in = NOW(), notes = VALUES(notes), processed_by = VALUES(processed_by)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.setString(2, notes);
            if (processedBy != null) {
                ps.setInt(3, processedBy);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            return ps.executeUpdate() > 0;
        }
    }

    public boolean recordCheckOut(int reservationId, String notes) throws SQLException {
        String sql = "UPDATE check_in_out SET actual_check_out = NOW(), notes = CONCAT(COALESCE(notes, ''), ' | Check-out: ', ?) " +
                     "WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, notes != null ? notes : "Completed");
            ps.setInt(2, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    private CheckInOut mapRow(ResultSet rs) throws SQLException {
        Timestamp inTs = rs.getTimestamp("actual_check_in");
        Timestamp outTs = rs.getTimestamp("actual_check_out");
        int procId = rs.getInt("processed_by");

        CheckInOut cio = new CheckInOut(
                rs.getInt("record_id"),
                rs.getInt("reservation_id"),
                inTs != null ? inTs.toLocalDateTime() : null,
                outTs != null ? outTs.toLocalDateTime() : null,
                rs.getString("notes"),
                rs.wasNull() ? null : procId
        );
        cio.setReservationCode(rs.getString("reservation_code"));
        cio.setCustomerName(rs.getString("customer_name"));
        cio.setRoomNumber(rs.getString("room_number"));
        cio.setProcessedByName(rs.getString("processed_by_name"));
        return cio;
    }
}
