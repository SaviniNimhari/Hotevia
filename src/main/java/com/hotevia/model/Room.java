package com.hotevia.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Room {
    private int roomId;
    private String roomNumber;
    private int categoryId;
    private String categoryName;
    private BigDecimal pricePerNight;
    private int floorNumber;
    private String status; // "AVAILABLE", "OCCUPIED", "RESERVED", "MAINTENANCE"
    private LocalDateTime createdAt;

    public Room() {
    }

    public Room(int roomId, String roomNumber, int categoryId, int floorNumber, String status, LocalDateTime createdAt) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.categoryId = categoryId;
        this.floorNumber = floorNumber;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Room(String roomNumber, int categoryId, int floorNumber, String status) {
        this.roomNumber = roomNumber;
        this.categoryId = categoryId;
        this.floorNumber = floorNumber;
        this.status = status;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + (categoryName != null ? categoryName : "Cat #" + categoryId) + ")";
    }
}
