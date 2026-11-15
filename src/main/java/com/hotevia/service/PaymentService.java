package com.hotevia.service;

import com.hotevia.dao.HotelSettingDAO;
import com.hotevia.dao.PaymentDAO;
import com.hotevia.dao.ReservationDAO;
import com.hotevia.model.HotelSetting;
import com.hotevia.model.Payment;
import com.hotevia.model.Reservation;
import com.hotevia.util.ReceiptGenerator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class PaymentService {

    private final PaymentDAO paymentDAO;
    private final ReservationDAO reservationDAO;
    private final HotelSettingDAO settingDAO;

    public PaymentService() {
        this.paymentDAO = new PaymentDAO();
        this.reservationDAO = new ReservationDAO();
        this.settingDAO = new HotelSettingDAO();
    }

    public void processPayment(Payment payment) throws SQLException {
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        Reservation res = reservationDAO.findById(payment.getReservationId());
        if (res == null) {
            throw new IllegalArgumentException("Reservation not found for payment.");
        }

        if (payment.getAmount().compareTo(res.getBalanceAmount()) > 0) {
            throw new IllegalArgumentException("Payment amount ($" + payment.getAmount() 
                    + ") exceeds outstanding balance ($" + res.getBalanceAmount() + ").");
        }

        paymentDAO.insert(payment);
        reservationDAO.updatePaidAmount(payment.getReservationId(), payment.getAmount());
    }

    public List<Payment> getPaymentsByReservation(int reservationId) throws SQLException {
        return paymentDAO.findByReservationId(reservationId);
    }

    public String generateReceipt(Payment payment) throws SQLException {
        Reservation res = reservationDAO.findById(payment.getReservationId());
        HotelSetting setting = settingDAO.getSettings();
        return ReceiptGenerator.generatePaymentReceipt(payment, res, setting);
    }
}
