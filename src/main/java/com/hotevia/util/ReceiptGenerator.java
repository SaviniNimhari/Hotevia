package com.hotevia.util;

import com.hotevia.model.HotelSetting;
import com.hotevia.model.Payment;
import com.hotevia.model.Reservation;

import java.time.format.DateTimeFormatter;

public class ReceiptGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public static String generateReservationReceipt(Reservation res, HotelSetting hotel) {
        String hName = hotel != null ? hotel.getHotelName() : "HOTEVIA GRAND LUXURY RESORT";
        String hAddr = hotel != null ? hotel.getAddress() : "120 Royal Palm Blvd, Coastal Bay";
        String hPhone = hotel != null ? hotel.getContactNumber() : "+1 (555) 019-2834";
        String cur = hotel != null ? hotel.getCurrency() : "$";

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================================\n");
        sb.append(centerText(hName, 57)).append("\n");
        sb.append(centerText(hAddr, 57)).append("\n");
        sb.append(centerText("Tel: " + hPhone, 57)).append("\n");
        sb.append("=========================================================\n");
        sb.append(centerText("OFFICIAL RESERVATION CONFIRMATION", 57)).append("\n");
        sb.append("=========================================================\n\n");

        sb.append(String.format("%-22s: %s\n", "Booking Reference", res.getReservationCode()));
        sb.append(String.format("%-22s: %s\n", "Guest Name", res.getCustomerName()));
        sb.append(String.format("%-22s: %s\n", "Contact Phone", res.getCustomerPhone() != null ? res.getCustomerPhone() : "N/A"));
        sb.append(String.format("%-22s: %s\n", "Room Assigned", "Room " + res.getRoomNumber() + " (" + res.getCategoryName() + ")"));
        sb.append(String.format("%-22s: %s\n", "Check-in Date", res.getCheckInDate() != null ? res.getCheckInDate().format(DATE_FMT) : ""));
        sb.append(String.format("%-22s: %s\n", "Check-out Date", res.getCheckOutDate() != null ? res.getCheckOutDate().format(DATE_FMT) : ""));
        sb.append(String.format("%-22s: %s\n", "Reservation Status", res.getStatus()));

        sb.append("\n---------------------------------------------------------\n");
        sb.append("BILLING SUMMARY\n");
        sb.append("---------------------------------------------------------\n");
        sb.append(String.format("%-35s: %s%10.2f\n", "Total Booking Cost", cur, res.getTotalAmount()));
        sb.append(String.format("%-35s: %s%10.2f\n", "Amount Paid / Deposit", cur, res.getPaidAmount() != null ? res.getPaidAmount() : 0.00));
        sb.append(String.format("%-35s: %s%10.2f\n", "Outstanding Balance", cur, res.getBalanceAmount()));
        sb.append("---------------------------------------------------------\n\n");

        sb.append("Thank you for choosing ").append(hName).append("!\n");
        sb.append("Standard check-in time is 14:00. Check-out time is 11:00.\n");
        sb.append("Please present this receipt upon check-in at front desk.\n");
        sb.append("=========================================================\n");

        return sb.toString();
    }

    public static String generatePaymentReceipt(Payment payment, Reservation res, HotelSetting hotel) {
        String hName = hotel != null ? hotel.getHotelName() : "HOTEVIA GRAND LUXURY RESORT";
        String cur = hotel != null ? hotel.getCurrency() : "$";

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================================\n");
        sb.append(centerText(hName, 57)).append("\n");
        sb.append(centerText("OFFICIAL PAYMENT RECEIPT", 57)).append("\n");
        sb.append("=========================================================\n\n");

        sb.append(String.format("%-22s: PAY-%06d\n", "Receipt No", payment.getPaymentId()));
        sb.append(String.format("%-22s: %s\n", "Date & Time", payment.getPaymentDate() != null ? payment.getPaymentDate().format(TIME_FMT) : "Just now"));
        sb.append(String.format("%-22s: %s\n", "Reservation Code", res != null ? res.getReservationCode() : String.valueOf(payment.getReservationId())));
        sb.append(String.format("%-22s: %s\n", "Customer Name", payment.getCustomerName() != null ? payment.getCustomerName() : (res != null ? res.getCustomerName() : "Guest")));
        sb.append(String.format("%-22s: %s\n", "Payment Method", payment.getPaymentMethod()));
        sb.append(String.format("%-22s: %s\n", "Payment Purpose / Note", payment.getNotes() != null ? payment.getNotes() : "Standard Room Charge"));

        sb.append("\n---------------------------------------------------------\n");
        sb.append(String.format("%-35s: %s%10.2f\n", "AMOUNT RECEIVED", cur, payment.getAmount()));
        if (res != null) {
            sb.append(String.format("%-35s: %s%10.2f\n", "Remaining Balance", cur, res.getBalanceAmount()));
        }
        sb.append("---------------------------------------------------------\n\n");
        sb.append("Payment successfully recorded in Hotevia Management System.\n");
        sb.append("This is an electronically generated official receipt.\n");
        sb.append("=========================================================\n");

        return sb.toString();
    }

    private static String centerText(String text, int width) {
        if (text == null) return "";
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }
}
