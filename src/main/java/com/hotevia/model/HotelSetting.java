package com.hotevia.model;

import java.math.BigDecimal;

public class HotelSetting {
    private int settingId;
    private String hotelName;
    private String address;
    private String contactNumber;
    private String email;
    private String currency;
    private BigDecimal taxRate;
    private BigDecimal serviceChargeRate;

    public HotelSetting() {
    }

    public HotelSetting(int settingId, String hotelName, String address, String contactNumber, String email,
                        String currency, BigDecimal taxRate, BigDecimal serviceChargeRate) {
        this.settingId = settingId;
        this.hotelName = hotelName;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
        this.currency = currency;
        this.taxRate = taxRate;
        this.serviceChargeRate = serviceChargeRate;
    }

    public int getSettingId() {
        return settingId;
    }

    public void setSettingId(int settingId) {
        this.settingId = settingId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getServiceChargeRate() {
        return serviceChargeRate;
    }

    public void setServiceChargeRate(BigDecimal serviceChargeRate) {
        this.serviceChargeRate = serviceChargeRate;
    }
}
