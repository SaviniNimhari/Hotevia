package com.hotevia.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+0-9\\-\\s()]{7,25}$");

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) return false;
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) return false;
        return checkOut.isAfter(checkIn);
    }

    public static boolean isPositiveDecimal(String value) {
        if (!isNotEmpty(value)) return false;
        try {
            BigDecimal bd = new BigDecimal(value.trim());
            return bd.compareTo(BigDecimal.ZERO) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isPositiveInteger(String value) {
        if (!isNotEmpty(value)) return false;
        try {
            int val = Integer.parseInt(value.trim());
            return val > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
