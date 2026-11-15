package com.hotevia.dao;

import com.hotevia.config.DatabaseConnection;
import com.hotevia.model.HotelSetting;

import java.math.BigDecimal;
import java.sql.*;

public class HotelSettingDAO {

    public HotelSetting getSettings() throws SQLException {
        String sql = "SELECT setting_id, hotel_name, address, contact_number, email, currency, tax_rate, service_charge_rate FROM hotel_settings LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new HotelSetting(
                        rs.getInt("setting_id"),
                        rs.getString("hotel_name"),
                        rs.getString("address"),
                        rs.getString("contact_number"),
                        rs.getString("email"),
                        rs.getString("currency"),
                        rs.getBigDecimal("tax_rate"),
                        rs.getBigDecimal("service_charge_rate")
                );
            }
        }
        return new HotelSetting(1, "Hotevia Grand Luxury Resort", "120 Royal Palm Blvd, Coastal Bay", "+1 (555) 019-2834", "reservations@hotevia.com", "$", new BigDecimal("10.00"), new BigDecimal("5.00"));
    }

    public boolean updateSettings(HotelSetting setting) throws SQLException {
        String sql = "UPDATE hotel_settings SET hotel_name = ?, address = ?, contact_number = ?, email = ?, " +
                     "currency = ?, tax_rate = ?, service_charge_rate = ? WHERE setting_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, setting.getHotelName());
            ps.setString(2, setting.getAddress());
            ps.setString(3, setting.getContactNumber());
            ps.setString(4, setting.getEmail());
            ps.setString(5, setting.getCurrency());
            ps.setBigDecimal(6, setting.getTaxRate());
            ps.setBigDecimal(7, setting.getServiceChargeRate());
            ps.setInt(8, setting.getSettingId());
            return ps.executeUpdate() > 0;
        }
    }
}
