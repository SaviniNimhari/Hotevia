package com.hotevia.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RoomCategory {
    private int categoryId;
    private String categoryName;
    private String description;
    private BigDecimal pricePerNight;
    private int capacity;
    private LocalDateTime createdAt;

    public RoomCategory() {
    }

    public RoomCategory(int categoryId, String categoryName, String description, BigDecimal pricePerNight, int capacity, LocalDateTime createdAt) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
        this.createdAt = createdAt;
    }

    public RoomCategory(String categoryName, String description, BigDecimal pricePerNight, int capacity) {
        this.categoryName = categoryName;
        this.description = description;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return categoryName + " ($" + pricePerNight + "/night, max " + capacity + " guests)";
    }
}
