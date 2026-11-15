package com.hotevia.dao;

import com.hotevia.config.DatabaseConnection;
import com.hotevia.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public List<Customer> findAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT customer_id, name, email, phone, address, id_card_or_passport, created_at FROM customers ORDER BY customer_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Customer findById(int customerId) throws SQLException {
        String sql = "SELECT customer_id, name, email, phone, address, id_card_or_passport, created_at FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Customer> search(String query) throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT customer_id, name, email, phone, address, id_card_or_passport, created_at FROM customers " +
                     "WHERE name LIKE ? OR phone LIKE ? OR email LIKE ? OR id_card_or_passport LIKE ? ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String wildcard = "%" + query.trim() + "%";
            ps.setString(1, wildcard);
            ps.setString(2, wildcard);
            ps.setString(3, wildcard);
            ps.setString(4, wildcard);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public boolean insert(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (name, email, phone, address, id_card_or_passport) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getAddress());
            ps.setString(5, customer.getIdCardOrPassport());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        customer.setCustomerId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ?, address = ?, id_card_or_passport = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getAddress());
            ps.setString(5, customer.getIdCardOrPassport());
            ps.setInt(6, customer.getCustomerId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int customerId) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new Customer(
                rs.getInt("customer_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("id_card_or_passport"),
                ts != null ? ts.toLocalDateTime() : null
        );
    }
}
