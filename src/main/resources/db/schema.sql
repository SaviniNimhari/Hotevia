-- ========================================================
-- Hotevia - Smart Hotel Reservation & Management System
-- Database Schema & Initial Seed Data
-- ========================================================

CREATE DATABASE IF NOT EXISTS hotevia_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hotevia_db;

-- 1. Hotel Settings Table
CREATE TABLE IF NOT EXISTS hotel_settings (
    setting_id INT AUTO_INCREMENT PRIMARY KEY,
    hotel_name VARCHAR(150) NOT NULL,
    address TEXT,
    contact_number VARCHAR(50),
    email VARCHAR(100),
    currency VARCHAR(10) DEFAULT '$',
    tax_rate DECIMAL(5,2) DEFAULT 10.00,
    service_charge_rate DECIMAL(5,2) DEFAULT 5.00,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. System Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'RECEPTIONIST') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Room Categories Table
CREATE TABLE IF NOT EXISTS room_categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    price_per_night DECIMAL(10,2) NOT NULL,
    capacity INT NOT NULL DEFAULT 2,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Rooms Table
CREATE TABLE IF NOT EXISTS rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(20) NOT NULL UNIQUE,
    category_id INT NOT NULL,
    floor_number INT DEFAULT 1,
    status ENUM('AVAILABLE', 'OCCUPIED', 'RESERVED', 'MAINTENANCE') DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rooms_category FOREIGN KEY (category_id) 
        REFERENCES room_categories(category_id) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- 5. Customers Table
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(30) NOT NULL,
    address TEXT,
    id_card_or_passport VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Reservations Table
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_code VARCHAR(30) NOT NULL UNIQUE,
    customer_id INT NOT NULL,
    room_id INT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    paid_amount DECIMAL(10,2) DEFAULT 0.00,
    status ENUM('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED') DEFAULT 'CONFIRMED',
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservations_customer FOREIGN KEY (customer_id) 
        REFERENCES customers(customer_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_reservations_room FOREIGN KEY (room_id) 
        REFERENCES rooms(room_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_reservations_user FOREIGN KEY (created_by) 
        REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE CASCADE
);

-- 7. Payments Table
CREATE TABLE IF NOT EXISTS payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'ONLINE_TRANSFER') NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(255),
    CONSTRAINT fk_payments_reservation FOREIGN KEY (reservation_id) 
        REFERENCES reservations(reservation_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- 8. Check-in and Check-out Records Table
CREATE TABLE IF NOT EXISTS check_in_out (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL UNIQUE,
    actual_check_in DATETIME,
    actual_check_out DATETIME,
    notes TEXT,
    processed_by INT,
    CONSTRAINT fk_checkin_reservation FOREIGN KEY (reservation_id) 
        REFERENCES reservations(reservation_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_checkin_user FOREIGN KEY (processed_by) 
        REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE CASCADE
);

-- ========================================================
-- SEED DATA
-- Default Passwords:
-- admin / admin123 (SHA-256: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9)
-- receptionist / recep123 (SHA-256: 9e35231c5b8882faae261778939c323f46f481abf541ee3cbe31c8153c393bc6)
-- ========================================================

INSERT INTO hotel_settings (setting_id, hotel_name, address, contact_number, email, currency, tax_rate, service_charge_rate)
VALUES (1, 'Hotevia Grand Luxury Resort', '120 Royal Palm Blvd, Coastal Bay', '+1 (555) 019-2834', 'reservations@hotevia.com', '$', 10.00, 5.00)
ON DUPLICATE KEY UPDATE hotel_name=VALUES(hotel_name);

INSERT INTO users (username, password_hash, full_name, role, status) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Master Administrator', 'ADMIN', 'ACTIVE'),
('receptionist', '9e35231c5b8882faae261778939c323f46f481abf541ee3cbe31c8153c393bc6', 'Elena Rostova', 'RECEPTIONIST', 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

INSERT INTO room_categories (category_id, category_name, description, price_per_night, capacity) VALUES
(1, 'Standard Room', 'Cozy single/double room equipped with high-speed WiFi, smart TV, queen bed, and modern ensuite bathroom.', 120.00, 2),
(2, 'Deluxe Room', 'Spacious executive room with panoramic city view, king bed, lounge area, and premium minibar.', 220.00, 3),
(3, 'Suite Room', 'Ultra-luxurious suite featuring private balcony, jacuzzi, walk-in closet, living room, and butler service.', 450.00, 4),
(4, 'Family Room', 'Spacious family apartment with 2 king beds, kitchenette, dining table, and kids entertainment station.', 320.00, 5)
ON DUPLICATE KEY UPDATE price_per_night=VALUES(price_per_night);

INSERT INTO rooms (room_number, category_id, floor_number, status) VALUES
('101', 1, 1, 'AVAILABLE'),
('102', 1, 1, 'AVAILABLE'),
('103', 1, 1, 'AVAILABLE'),
('104', 1, 1, 'MAINTENANCE'),
('201', 2, 2, 'AVAILABLE'),
('202', 2, 2, 'OCCUPIED'),
('203', 2, 2, 'AVAILABLE'),
('204', 2, 2, 'RESERVED'),
('301', 3, 3, 'AVAILABLE'),
('302', 3, 3, 'OCCUPIED'),
('401', 4, 4, 'AVAILABLE'),
('402', 4, 4, 'AVAILABLE')
ON DUPLICATE KEY UPDATE status=VALUES(status);

INSERT INTO customers (customer_id, name, email, phone, address, id_card_or_passport) VALUES
(1, 'Jonathan Carter', 'jonathan.carter@email.com', '+1 (555) 234-5678', '45 Elm Street, Seattle, WA', 'US-P8923481'),
(2, 'Sophia Martinez', 'sophia.m@luxurytravel.org', '+1 (555) 345-6789', '88 Sunset Blvd, Los Angeles, CA', 'US-P4459812'),
(3, 'Alexander Wright', 'alex.wright@techventures.io', '+44 20 7946 0912', '12 Kensington High St, London', 'UK-C9012384')
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO reservations (reservation_id, reservation_code, customer_id, room_id, check_in_date, check_out_date, total_amount, paid_amount, status, created_by) VALUES
(1, 'HTV-2026-001', 1, 5, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 660.00, 660.00, 'CHECKED_IN', 1),
(2, 'HTV-2026-002', 2, 8, DATE_ADD(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 4 DAY), 660.00, 200.00, 'CONFIRMED', 2),
(3, 'HTV-2026-003', 3, 10, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 900.00, 900.00, 'CHECKED_IN', 1)
ON DUPLICATE KEY UPDATE total_amount=VALUES(total_amount);

INSERT INTO payments (reservation_id, amount, payment_method, notes) VALUES
(1, 660.00, 'CREDIT_CARD', 'Full advance settlement at check-in'),
(2, 200.00, 'ONLINE_TRANSFER', 'Deposit payment via bank transfer'),
(3, 900.00, 'CREDIT_CARD', 'Corporate account payment')
ON DUPLICATE KEY UPDATE amount=VALUES(amount);

INSERT INTO check_in_out (reservation_id, actual_check_in, actual_check_out, notes, processed_by) VALUES
(1, NOW(), NULL, 'Guest requested extra pillows and late breakfast', 2),
(3, NOW(), NULL, 'VIP guest welcomed with fruit basket', 1)
ON DUPLICATE KEY UPDATE notes=VALUES(notes);
