package com.hotevia.dao;

import com.hotevia.config.DatabaseConnection;
import com.hotevia.model.Payment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public List<Payment> findAll() throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.payment_id, p.reservation_id, p.amount, p.payment_method, p.payment_date, p.notes, " +
                     "r.reservation_code, c.name AS customer_name " +
                     "FROM payments p " +
                     "JOIN reservations r ON p.reservation_id = r.reservation_id " +
                     "JOIN customers c ON r.customer_id = c.customer_id " +
                     "ORDER BY p.payment_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Payment> findByReservationId(int reservationId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.payment_id, p.reservation_id, p.amount, p.payment_method, p.payment_date, p.notes, " +
                     "r.reservation_code, c.name AS customer_name " +
                     "FROM payments p " +
                     "JOIN reservations r ON p.reservation_id = r.reservation_id " +
                     "JOIN customers c ON r.customer_id = c.customer_id " +
                     "WHERE p.reservation_id = ? " +
                     "ORDER BY p.payment_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public boolean insert(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (reservation_id, amount, payment_method, notes) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getReservationId());
            ps.setBigDecimal(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMethod());
            ps.setString(4, payment.getNotes());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        payment.setPaymentId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("payment_date");
        Payment p = new Payment(
                rs.getInt("payment_id"),
                rs.getInt("reservation_id"),
                rs.getBigDecimal("amount"),
                rs.getString("payment_method"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getString("notes")
        );
        p.setReservationCode(rs.getString("reservation_code"));
        p.setCustomerName(rs.getString("customer_name"));
        return p;
    }
}
