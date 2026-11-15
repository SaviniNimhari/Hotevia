package com.hotevia.dao;

import com.hotevia.config.DatabaseConnection;
import com.hotevia.model.Reservation;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public List<Reservation> findAll() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = baseQuery() + " ORDER BY res.reservation_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Reservation findById(int reservationId) throws SQLException {
        String sql = baseQuery() + " WHERE res.reservation_id = ?";
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

    public Reservation findByCode(String reservationCode) throws SQLException {
        String sql = baseQuery() + " WHERE res.reservation_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reservationCode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Prevents overlapping bookings for the same room.
     */
    public boolean isRoomAvailable(int roomId, LocalDate checkIn, LocalDate checkOut, Integer excludeReservationId) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM reservations res " +
            "WHERE res.room_id = ? " +
            "AND res.status IN ('CONFIRMED', 'CHECKED_IN') " +
            "AND (res.check_in_date < ? AND res.check_out_date > ?) "
        );
        if (excludeReservationId != null) {
            sql.append("AND res.reservation_id != ?");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, roomId);
            ps.setDate(2, Date.valueOf(checkOut));
            ps.setDate(3, Date.valueOf(checkIn));
            if (excludeReservationId != null) {
                ps.setInt(4, excludeReservationId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return true;
    }

    public boolean insert(Reservation res) throws SQLException {
        String sql = "INSERT INTO reservations (reservation_code, customer_id, room_id, check_in_date, check_out_date, " +
                     "total_amount, paid_amount, status, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, res.getReservationCode());
            ps.setInt(2, res.getCustomerId());
            ps.setInt(3, res.getRoomId());
            ps.setDate(4, Date.valueOf(res.getCheckInDate()));
            ps.setDate(5, Date.valueOf(res.getCheckOutDate()));
            ps.setBigDecimal(6, res.getTotalAmount());
            ps.setBigDecimal(7, res.getPaidAmount() != null ? res.getPaidAmount() : BigDecimal.ZERO);
            ps.setString(8, res.getStatus() != null ? res.getStatus() : "CONFIRMED");
            if (res.getCreatedBy() != null) {
                ps.setInt(9, res.getCreatedBy());
            } else {
                ps.setNull(9, Types.INTEGER);
            }

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        res.setReservationId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Reservation res) throws SQLException {
        String sql = "UPDATE reservations SET customer_id = ?, room_id = ?, check_in_date = ?, check_out_date = ?, " +
                     "total_amount = ?, paid_amount = ?, status = ? WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, res.getCustomerId());
            ps.setInt(2, res.getRoomId());
            ps.setDate(3, Date.valueOf(res.getCheckInDate()));
            ps.setDate(4, Date.valueOf(res.getCheckOutDate()));
            ps.setBigDecimal(5, res.getTotalAmount());
            ps.setBigDecimal(6, res.getPaidAmount());
            ps.setString(7, res.getStatus());
            ps.setInt(8, res.getReservationId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int reservationId, String status) throws SQLException {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePaidAmount(int reservationId, BigDecimal addAmount) throws SQLException {
        String sql = "UPDATE reservations SET paid_amount = paid_amount + ? WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, addAmount);
            ps.setInt(2, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    public int countTotalReservations() throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countTodayCheckIns() throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE check_in_date = CURDATE() AND status IN ('CONFIRMED', 'CHECKED_IN')";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countTodayCheckOuts() throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE check_out_date = CURDATE() AND status = 'CHECKED_IN'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public BigDecimal getTotalRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getBigDecimal(1);
        }
        return BigDecimal.ZERO;
    }

    private String baseQuery() {
        return "SELECT res.reservation_id, res.reservation_code, res.customer_id, res.room_id, " +
               "res.check_in_date, res.check_out_date, res.total_amount, res.paid_amount, res.status, " +
               "res.created_by, res.created_at, " +
               "c.name AS customer_name, c.phone AS customer_phone, " +
               "r.room_number, cat.category_name, u.full_name AS created_by_name " +
               "FROM reservations res " +
               "JOIN customers c ON res.customer_id = c.customer_id " +
               "JOIN rooms r ON res.room_id = r.room_id " +
               "JOIN room_categories cat ON r.category_id = cat.category_id " +
               "LEFT JOIN users u ON res.created_by = u.user_id";
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Date in = rs.getDate("check_in_date");
        Date out = rs.getDate("check_out_date");
        Timestamp ts = rs.getTimestamp("created_at");
        int createdById = rs.getInt("created_by");

        Reservation res = new Reservation(
                rs.getInt("reservation_id"),
                rs.getString("reservation_code"),
                rs.getInt("customer_id"),
                rs.getInt("room_id"),
                in != null ? in.toLocalDate() : null,
                out != null ? out.toLocalDate() : null,
                rs.getBigDecimal("total_amount"),
                rs.getBigDecimal("paid_amount"),
                rs.getString("status"),
                rs.wasNull() ? null : createdById,
                ts != null ? ts.toLocalDateTime() : null
        );

        res.setCustomerName(rs.getString("customer_name"));
        res.setCustomerPhone(rs.getString("customer_phone"));
        res.setRoomNumber(rs.getString("room_number"));
        res.setCategoryName(rs.getString("category_name"));
        res.setCreatedByName(rs.getString("created_by_name"));
        return res;
    }
}
