package com.hotevia.service;

import com.hotevia.config.DatabaseConnection;
import com.hotevia.dao.ReservationDAO;
import com.hotevia.dao.RoomDAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportService {

    private final RoomDAO roomDAO;
    private final ReservationDAO reservationDAO;

    public ReportService() {
        this.roomDAO = new RoomDAO();
        this.reservationDAO = new ReservationDAO();
    }

    public Map<String, Integer> getRoomStatusCounts() throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("Available", roomDAO.countRoomsByStatus("AVAILABLE"));
        stats.put("Occupied", roomDAO.countRoomsByStatus("OCCUPIED"));
        stats.put("Reserved", roomDAO.countRoomsByStatus("RESERVED"));
        stats.put("Maintenance", roomDAO.countRoomsByStatus("MAINTENANCE"));
        return stats;
    }

    public Map<String, BigDecimal> getRevenueByPaymentMethod() throws SQLException {
        Map<String, BigDecimal> data = new LinkedHashMap<>();
        String sql = "SELECT payment_method, COALESCE(SUM(amount), 0) AS total FROM payments GROUP BY payment_method";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("payment_method"), rs.getBigDecimal("total"));
            }
        }
        return data;
    }

    public Map<String, BigDecimal> getMonthlyRevenueForYear(int year) throws SQLException {
        Map<String, BigDecimal> data = new LinkedHashMap<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (String m : months) {
            data.put(m, BigDecimal.ZERO);
        }

        String sql = "SELECT MONTH(payment_date) AS m, SUM(amount) AS total " +
                     "FROM payments WHERE YEAR(payment_date) = " + year + " " +
                     "GROUP BY MONTH(payment_date) ORDER BY m ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int mIdx = rs.getInt("m") - 1;
                if (mIdx >= 0 && mIdx < 12) {
                    data.put(months[mIdx], rs.getBigDecimal("total"));
                }
            }
        }
        return data;
    }

    public double getOccupancyRate() throws SQLException {
        int total = roomDAO.countTotalRooms();
        if (total == 0) return 0.0;
        int occupied = roomDAO.countRoomsByStatus("OCCUPIED");
        return ((double) occupied / total) * 100.0;
    }
}
