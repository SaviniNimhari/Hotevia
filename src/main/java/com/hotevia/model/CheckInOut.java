package com.hotevia.model;

import java.time.LocalDateTime;

public class CheckInOut {
    private int recordId;
    private int reservationId;
    private String reservationCode;
    private String customerName;
    private String roomNumber;
    private LocalDateTime actualCheckIn;
    private LocalDateTime actualCheckOut;
    private String notes;
    private Integer processedBy;
    private String processedByName;

    public CheckInOut() {
    }

    public CheckInOut(int recordId, int reservationId, LocalDateTime actualCheckIn, LocalDateTime actualCheckOut, String notes, Integer processedBy) {
        this.recordId = recordId;
        this.reservationId = reservationId;
        this.actualCheckIn = actualCheckIn;
        this.actualCheckOut = actualCheckOut;
        this.notes = notes;
        this.processedBy = processedBy;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public String getReservationCode() {
        return reservationCode;
    }

    public void setReservationCode(String reservationCode) {
        this.reservationCode = reservationCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public LocalDateTime getActualCheckIn() {
        return actualCheckIn;
    }

    public void setActualCheckIn(LocalDateTime actualCheckIn) {
        this.actualCheckIn = actualCheckIn;
    }

    public LocalDateTime getActualCheckOut() {
        return actualCheckOut;
    }

    public void setActualCheckOut(LocalDateTime actualCheckOut) {
        this.actualCheckOut = actualCheckOut;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Integer processedBy) {
        this.processedBy = processedBy;
    }

    public String getProcessedByName() {
        return processedByName;
    }

    public void setProcessedByName(String processedByName) {
        this.processedByName = processedByName;
    }
}
