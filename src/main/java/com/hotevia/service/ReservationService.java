package com.hotevia.service;

import com.hotevia.dao.CheckInOutDAO;
import com.hotevia.dao.HotelSettingDAO;
import com.hotevia.dao.ReservationDAO;
import com.hotevia.dao.RoomDAO;
import com.hotevia.model.HotelSetting;
import com.hotevia.model.Reservation;
import com.hotevia.model.Room;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public class ReservationService {

    private final ReservationDAO reservationDAO;
    private final RoomDAO roomDAO;
    private final CheckInOutDAO checkInOutDAO;
    private final HotelSettingDAO settingDAO;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.roomDAO = new RoomDAO();
        this.checkInOutDAO = new CheckInOutDAO();
        this.settingDAO = new HotelSettingDAO();
    }

    public String generateUniqueReservationCode() {
        int year = Year.now().getValue();
        int randomDigits = 1000 + new Random().nextInt(9000);
        return String.format("HTV-%d-%04d", year, randomDigits);
    }

    public BigDecimal calculateCost(BigDecimal pricePerNight, LocalDate checkIn, LocalDate checkOut) throws SQLException {
        if (pricePerNight == null || checkIn == null || checkOut == null) {
            return BigDecimal.ZERO;
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) {
            nights = 1;
        }

        BigDecimal subtotal = pricePerNight.multiply(BigDecimal.valueOf(nights));
        HotelSetting setting = settingDAO.getSettings();

        BigDecimal taxRate = setting.getTaxRate() != null ? setting.getTaxRate() : BigDecimal.ZERO;
        BigDecimal serviceRate = setting.getServiceChargeRate() != null ? setting.getServiceChargeRate() : BigDecimal.ZERO;

        BigDecimal combinedRate = taxRate.add(serviceRate).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal extraCharges = subtotal.multiply(combinedRate);

        return subtotal.add(extraCharges).setScale(2, RoundingMode.HALF_UP);
    }

    public void createReservation(Reservation res) throws SQLException {
        if (res.getCheckInDate() == null || res.getCheckOutDate() == null) {
            throw new IllegalArgumentException("Check-in and Check-out dates are required.");
        }
        if (!res.getCheckOutDate().isAfter(res.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }

        // DOUBLE BOOKING CHECK
        boolean isAvailable = reservationDAO.isRoomAvailable(
                res.getRoomId(), res.getCheckInDate(), res.getCheckOutDate(), null);

        if (!isAvailable) {
            throw new IllegalStateException("Room is already reserved or occupied for the selected dates! Double booking prevented.");
        }

        if (res.getReservationCode() == null || res.getReservationCode().isEmpty()) {
            res.setReservationCode(generateUniqueReservationCode());
        }

        reservationDAO.insert(res);

        // Update room status if check-in is today
        if (res.getCheckInDate().equals(LocalDate.now())) {
            roomDAO.updateStatus(res.getRoomId(), "RESERVED");
        }
    }

    public void checkInCustomer(int reservationId, String notes, Integer processedBy) throws SQLException {
        Reservation res = reservationDAO.findById(reservationId);
        if (res == null) {
            throw new IllegalArgumentException("Reservation not found.");
        }

        if ("CHECKED_IN".equalsIgnoreCase(res.getStatus())) {
            throw new IllegalStateException("Guest is already checked in.");
        }
        if ("CANCELLED".equalsIgnoreCase(res.getStatus()) || "CHECKED_OUT".equalsIgnoreCase(res.getStatus())) {
            throw new IllegalStateException("Cannot check-in a " + res.getStatus() + " reservation.");
        }

        reservationDAO.updateStatus(reservationId, "CHECKED_IN");
        roomDAO.updateStatus(res.getRoomId(), "OCCUPIED");
        checkInOutDAO.recordCheckIn(reservationId, notes, processedBy);
    }

    public void checkOutCustomer(int reservationId, String notes) throws SQLException {
        Reservation res = reservationDAO.findById(reservationId);
        if (res == null) {
            throw new IllegalArgumentException("Reservation not found.");
        }

        if (!"CHECKED_IN".equalsIgnoreCase(res.getStatus())) {
            throw new IllegalStateException("Only checked-in guests can be checked out.");
        }

        if (res.getBalanceAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Guest has an outstanding balance of $" + res.getBalanceAmount() + ". Please settle payment before checkout.");
        }

        reservationDAO.updateStatus(reservationId, "CHECKED_OUT");
        roomDAO.updateStatus(res.getRoomId(), "AVAILABLE");
        checkInOutDAO.recordCheckOut(reservationId, notes);
    }

    public void cancelReservation(int reservationId) throws SQLException {
        Reservation res = reservationDAO.findById(reservationId);
        if (res == null) {
            throw new IllegalArgumentException("Reservation not found.");
        }

        if ("CHECKED_IN".equalsIgnoreCase(res.getStatus()) || "CHECKED_OUT".equalsIgnoreCase(res.getStatus())) {
            throw new IllegalStateException("Cannot cancel a reservation that is already " + res.getStatus());
        }

        reservationDAO.updateStatus(reservationId, "CANCELLED");
        // Release room status back to AVAILABLE if it was RESERVED
        Room room = roomDAO.findById(res.getRoomId());
        if (room != null && "RESERVED".equalsIgnoreCase(room.getStatus())) {
            roomDAO.updateStatus(res.getRoomId(), "AVAILABLE");
        }
    }
}
