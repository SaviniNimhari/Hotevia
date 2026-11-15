package com.hotevia.controller;

import com.hotevia.dao.RoomCategoryDAO;
import com.hotevia.dao.RoomDAO;
import com.hotevia.model.Room;
import com.hotevia.model.RoomCategory;
import com.hotevia.util.AlertUtil;
import com.hotevia.util.ValidationUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.List;

public class RoomController {

    @FXML private TableView<Room> roomTable;
    @FXML private TableColumn<Room, String> colRoomNum;
    @FXML private TableColumn<Room, String> colCategory;
    @FXML private TableColumn<Room, Integer> colFloor;
    @FXML private TableColumn<Room, BigDecimal> colPrice;
    @FXML private TableColumn<Room, String> colStatus;

    @FXML private TextField txtRoomNumber;
    @FXML private ComboBox<RoomCategory> cmbCategory;
    @FXML private TextField txtFloor;
    @FXML private ComboBox<String> cmbStatus;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilterStatus;

    private final RoomDAO roomDAO = new RoomDAO();
    private final RoomCategoryDAO categoryDAO = new RoomCategoryDAO();
    private final ObservableList<Room> masterRoomList = FXCollections.observableArrayList();
    private Room selectedRoom = null;

    @FXML
    public void initialize() {
        colRoomNum.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colFloor.setCellValueFactory(new PropertyValueFactory<>("floorNumber"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        cmbStatus.setItems(FXCollections.observableArrayList("AVAILABLE", "OCCUPIED", "RESERVED", "MAINTENANCE"));
        cmbStatus.setValue("AVAILABLE");

        cmbFilterStatus.setItems(FXCollections.observableArrayList("ALL", "AVAILABLE", "OCCUPIED", "RESERVED", "MAINTENANCE"));
        cmbFilterStatus.setValue("ALL");

        roomTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });

        loadCategories();
        loadRooms();
    }

    private void loadCategories() {
        try {
            List<RoomCategory> categories = categoryDAO.findAll();
            cmbCategory.setItems(FXCollections.observableArrayList(categories));
            if (!categories.isEmpty()) {
                cmbCategory.setValue(categories.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRooms() {
        new Thread(() -> {
            try {
                List<Room> rooms = roomDAO.findAll();
                Platform.runLater(() -> {
                    masterRoomList.setAll(rooms);
                    applyFilter();
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Data Error", "Could not load rooms", e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void applyFilter() {
        String query = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        String statusFilter = cmbFilterStatus.getValue();

        ObservableList<Room> filtered = masterRoomList.filtered(r -> {
            boolean matchesSearch = query.isEmpty() ||
                    r.getRoomNumber().toLowerCase().contains(query) ||
                    (r.getCategoryName() != null && r.getCategoryName().toLowerCase().contains(query));

            boolean matchesStatus = "ALL".equalsIgnoreCase(statusFilter) ||
                    r.getStatus().equalsIgnoreCase(statusFilter);

            return matchesSearch && matchesStatus;
        });

        roomTable.setItems(filtered);
    }

    private void populateForm(Room room) {
        selectedRoom = room;
        txtRoomNumber.setText(room.getRoomNumber());
        txtFloor.setText(String.valueOf(room.getFloorNumber()));
        cmbStatus.setValue(room.getStatus());

        for (RoomCategory cat : cmbCategory.getItems()) {
            if (cat.getCategoryId() == room.getCategoryId()) {
                cmbCategory.setValue(cat);
                break;
            }
        }
    }

    @FXML
    public void handleSave() {
        String roomNum = txtRoomNumber.getText();
        RoomCategory cat = cmbCategory.getValue();
        String floorStr = txtFloor.getText();
        String status = cmbStatus.getValue();

        if (!ValidationUtil.isNotEmpty(roomNum)) {
            AlertUtil.showWarning("Validation", "Room Number Required", "Please enter a valid room number.");
            return;
        }
        if (cat == null) {
            AlertUtil.showWarning("Validation", "Category Required", "Please select a room category.");
            return;
        }

        int floor = 1;
        if (ValidationUtil.isPositiveInteger(floorStr)) {
            floor = Integer.parseInt(floorStr.trim());
        }

        try {
            if (selectedRoom == null) {
                // New Room
                Room r = new Room(roomNum.trim(), cat.getCategoryId(), floor, status);
                boolean ok = roomDAO.insert(r);
                if (ok) {
                    AlertUtil.showInfo("Success", "Room Added", "Room " + roomNum + " successfully created.");
                    handleClear();
                    loadRooms();
                }
            } else {
                // Update
                selectedRoom.setRoomNumber(roomNum.trim());
                selectedRoom.setCategoryId(cat.getCategoryId());
                selectedRoom.setFloorNumber(floor);
                selectedRoom.setStatus(status);
                boolean ok = roomDAO.update(selectedRoom);
                if (ok) {
                    AlertUtil.showInfo("Success", "Room Updated", "Room " + roomNum + " successfully updated.");
                    handleClear();
                    loadRooms();
                }
            }
        } catch (Exception e) {
            AlertUtil.showError("Save Error", "Could not save room", e.getMessage());
        }
    }

    @FXML
    public void handleDelete() {
        if (selectedRoom == null) {
            AlertUtil.showWarning("Selection Required", "No Room Selected", "Select a room from the table to delete.");
            return;
        }

        boolean confirm = AlertUtil.showConfirmation("Confirm Delete", "Delete Room " + selectedRoom.getRoomNumber() + "?",
                "This action cannot be undone if the room has no active reservations.");
        if (confirm) {
            try {
                boolean ok = roomDAO.delete(selectedRoom.getRoomId());
                if (ok) {
                    AlertUtil.showInfo("Deleted", "Room Removed", "Room deleted successfully.");
                    handleClear();
                    loadRooms();
                }
            } catch (Exception e) {
                AlertUtil.showError("Delete Error", "Cannot delete room", "Room may be linked to existing reservations.");
            }
        }
    }

    @FXML
    public void handleClear() {
        selectedRoom = null;
        txtRoomNumber.clear();
        txtFloor.clear();
        cmbStatus.setValue("AVAILABLE");
        roomTable.getSelectionModel().clearSelection();
    }
}
