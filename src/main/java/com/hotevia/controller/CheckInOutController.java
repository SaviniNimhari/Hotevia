package com.hotevia.controller;

import com.hotevia.dao.CheckInOutDAO;
import com.hotevia.dao.ReservationDAO;
import com.hotevia.model.CheckInOut;
import com.hotevia.model.Reservation;
import com.hotevia.service.AuthenticationService;
import com.hotevia.service.ReservationService;
import com.hotevia.util.AlertUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CheckInOutController {

    @FXML private TableView<Reservation> activeReservationsTable;
    @FXML private TableColumn<Reservation, String> colResCode;
    @FXML private TableColumn<Reservation, String> colCustomer;
    @FXML private TableColumn<Reservation, String> colRoom;
    @FXML private TableColumn<Reservation, String> colDates;
    @FXML private TableColumn<Reservation, BigDecimal> colBalance;
    @FXML private TableColumn<Reservation, String> colStatus;

    @FXML private TableView<CheckInOut> historyLogTable;
    @FXML private TableColumn<CheckInOut, String> colLogRes;
    @FXML private TableColumn<CheckInOut, String> colLogCustomer;
    @FXML private TableColumn<CheckInOut, String> colLogRoom;
    @FXML private TableColumn<CheckInOut, LocalDateTime> colLogCheckIn;
    @FXML private TableColumn<CheckInOut, LocalDateTime> colLogCheckOut;
    @FXML private TableColumn<CheckInOut, String> colLogNotes;

    @FXML private TextField txtSearchRes;
    @FXML private TextArea txtOperationalNotes;
    @FXML private Label lblSelectedDetails;

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final CheckInOutDAO checkInOutDAO = new CheckInOutDAO();
    private final ReservationService reservationService = new ReservationService();

    private Reservation selectedReservation = null;

    @FXML
    public void initialize() {
        colResCode.setCellValueFactory(new PropertyValueFactory<>("reservationCode"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colDates.setCellValueFactory(cellData -> {
            Reservation r = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    r.getCheckInDate() + " -> " + r.getCheckOutDate());
        });
        colBalance.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getBalanceAmount()));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colLogRes.setCellValueFactory(new PropertyValueFactory<>("reservationCode"));
        colLogCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colLogRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colLogCheckIn.setCellValueFactory(new PropertyValueFactory<>("actualCheckIn"));
        colLogCheckOut.setCellValueFactory(new PropertyValueFactory<>("actualCheckOut"));
        colLogNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        activeReservationsTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                selectedReservation = n;
                lblSelectedDetails.setText(String.format("Selected: %s | Guest: %s | Room: %s | Bal: $%s | Status: %s",
                        n.getReservationCode(), n.getCustomerName(), n.getRoomNumber(), n.getBalanceAmount(), n.getStatus()));
            }
        });

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                List<Reservation> all = reservationDAO.findAll();
                List<Reservation> eligible = all.stream()
                        .filter(r -> "CONFIRMED".equalsIgnoreCase(r.getStatus()) || "CHECKED_IN".equalsIgnoreCase(r.getStatus()))
                        .toList();

                List<CheckInOut> logs = checkInOutDAO.findAll();

                Platform.runLater(() -> {
                    activeReservationsTable.setItems(FXCollections.observableArrayList(eligible));
                    historyLogTable.setItems(FXCollections.observableArrayList(logs));
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Data Error", "Could not load check-in data", e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void handleSearch() {
        String query = txtSearchRes.getText();
        if (query == null || query.trim().isEmpty()) {
            loadData();
            return;
        }

        new Thread(() -> {
            try {
                Reservation found = reservationDAO.findByCode(query.trim());
                Platform.runLater(() -> {
                    if (found != null) {
                        activeReservationsTable.setItems(FXCollections.observableArrayList(List.of(found)));
                    } else {
                        activeReservationsTable.setItems(FXCollections.emptyObservableList());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    public void handleCheckIn() {
        if (selectedReservation == null) {
            AlertUtil.showWarning("Selection Required", "No Reservation Selected", "Please select a booking to check-in.");
            return;
        }

        String notes = txtOperationalNotes.getText();
        Integer userId = (AuthenticationService.getCurrentUser() != null) ? AuthenticationService.getCurrentUser().getUserId() : null;

        try {
            reservationService.checkInCustomer(selectedReservation.getReservationId(), notes, userId);
            AlertUtil.showInfo("Check-in Successful", "Guest Arrival Confirmed",
                    selectedReservation.getCustomerName() + " has been checked into Room " + selectedReservation.getRoomNumber());
            txtOperationalNotes.clear();
            loadData();
        } catch (Exception e) {
            AlertUtil.showError("Check-in Failed", "Operation Denied", e.getMessage());
        }
    }

    @FXML
    public void handleCheckOut() {
        if (selectedReservation == null) {
            AlertUtil.showWarning("Selection Required", "No Reservation Selected", "Please select a booking to check-out.");
            return;
        }

        String notes = txtOperationalNotes.getText();

        try {
            reservationService.checkOutCustomer(selectedReservation.getReservationId(), notes);
            AlertUtil.showInfo("Check-out Successful", "Guest Departure Completed",
                    selectedReservation.getCustomerName() + " has checked out. Room " + selectedReservation.getRoomNumber() + " is now available for housekeeping.");
            txtOperationalNotes.clear();
            loadData();
        } catch (Exception e) {
            AlertUtil.showError("Check-out Blocked", "Unsettled Balance or Invalid State", e.getMessage());
        }
    }
}
