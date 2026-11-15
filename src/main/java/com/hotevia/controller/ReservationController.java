package com.hotevia.controller;

import com.hotevia.dao.CustomerDAO;
import com.hotevia.dao.HotelSettingDAO;
import com.hotevia.dao.ReservationDAO;
import com.hotevia.dao.RoomDAO;
import com.hotevia.model.Customer;
import com.hotevia.model.HotelSetting;
import com.hotevia.model.Reservation;
import com.hotevia.model.Room;
import com.hotevia.service.AuthenticationService;
import com.hotevia.service.ReservationService;
import com.hotevia.util.AlertUtil;
import com.hotevia.util.ReceiptGenerator;
import com.hotevia.util.ValidationUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReservationController {

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, String> colCode;
    @FXML private TableColumn<Reservation, String> colCustomer;
    @FXML private TableColumn<Reservation, String> colRoom;
    @FXML private TableColumn<Reservation, LocalDate> colCheckIn;
    @FXML private TableColumn<Reservation, LocalDate> colCheckOut;
    @FXML private TableColumn<Reservation, BigDecimal> colTotal;
    @FXML private TableColumn<Reservation, BigDecimal> colPaid;
    @FXML private TableColumn<Reservation, String> colStatus;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilterStatus;

    // Form inputs
    @FXML private ComboBox<Customer> cmbCustomer;
    @FXML private DatePicker dpCheckIn;
    @FXML private DatePicker dpCheckOut;
    @FXML private ComboBox<Room> cmbRoom;
    @FXML private Label lblCalculatedTotal;
    @FXML private TextField txtDepositPaid;
    @FXML private ComboBox<String> cmbStatus;

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final HotelSettingDAO settingDAO = new HotelSettingDAO();
    private final ReservationService reservationService = new ReservationService();

    private final ObservableList<Reservation> masterList = FXCollections.observableArrayList();
    private Reservation selectedReservation = null;

    @FXML
    public void initialize() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("reservationCode"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        cmbStatus.setItems(FXCollections.observableArrayList("CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED"));
        cmbStatus.setValue("CONFIRMED");

        cmbFilterStatus.setItems(FXCollections.observableArrayList("ALL", "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED"));
        cmbFilterStatus.setValue("ALL");

        // Set default dates
        dpCheckIn.setValue(LocalDate.now());
        dpCheckOut.setValue(LocalDate.now().plusDays(2));

        // Date change listeners for availability and cost recalculation
        dpCheckIn.valueProperty().addListener((obs, o, n) -> onDatesChanged());
        dpCheckOut.valueProperty().addListener((obs, o, n) -> onDatesChanged());
        cmbRoom.valueProperty().addListener((obs, o, n) -> calculateCost());

        reservationTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                populateForm(n);
            }
        });

        loadCustomers();
        loadReservations();
        onDatesChanged();
    }

    private void loadCustomers() {
        try {
            List<Customer> customers = customerDAO.findAll();
            cmbCustomer.setItems(FXCollections.observableArrayList(customers));
            if (!customers.isEmpty()) {
                cmbCustomer.setValue(customers.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadReservations() {
        new Thread(() -> {
            try {
                List<Reservation> list = reservationDAO.findAll();
                Platform.runLater(() -> {
                    masterList.setAll(list);
                    applyFilter();
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Error", "Could not load reservations", e.getMessage()));
            }
        }).start();
    }

    private void onDatesChanged() {
        LocalDate in = dpCheckIn.getValue();
        LocalDate out = dpCheckOut.getValue();

        if (in != null && out != null && out.isAfter(in)) {
            new Thread(() -> {
                try {
                    Integer excludeId = (selectedReservation != null) ? selectedReservation.getReservationId() : null;
                    List<Room> available = roomDAO.findAvailableRoomsForDates(in, out, excludeId);
                    Platform.runLater(() -> {
                        Room currentSelected = cmbRoom.getValue();
                        cmbRoom.setItems(FXCollections.observableArrayList(available));
                        if (currentSelected != null && available.stream().anyMatch(r -> r.getRoomId() == currentSelected.getRoomId())) {
                            cmbRoom.setValue(currentSelected);
                        } else if (!available.isEmpty()) {
                            cmbRoom.setValue(available.get(0));
                        } else {
                            cmbRoom.setValue(null);
                        }
                        calculateCost();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            lblCalculatedTotal.setText("$0.00");
        }
    }

    private void calculateCost() {
        Room room = cmbRoom.getValue();
        LocalDate in = dpCheckIn.getValue();
        LocalDate out = dpCheckOut.getValue();

        if (room != null && in != null && out != null && out.isAfter(in)) {
            try {
                BigDecimal total = reservationService.calculateCost(room.getPricePerNight(), in, out);
                lblCalculatedTotal.setText("$" + total);
            } catch (Exception e) {
                lblCalculatedTotal.setText("$0.00");
            }
        } else {
            lblCalculatedTotal.setText("$0.00");
        }
    }

    @FXML
    public void applyFilter() {
        String query = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        String status = cmbFilterStatus.getValue();

        ObservableList<Reservation> filtered = masterList.filtered(r -> {
            boolean matchesSearch = query.isEmpty() ||
                    r.getReservationCode().toLowerCase().contains(query) ||
                    r.getCustomerName().toLowerCase().contains(query) ||
                    r.getRoomNumber().toLowerCase().contains(query);

            boolean matchesStatus = "ALL".equalsIgnoreCase(status) ||
                    r.getStatus().equalsIgnoreCase(status);

            return matchesSearch && matchesStatus;
        });

        reservationTable.setItems(filtered);
    }

    private void populateForm(Reservation res) {
        selectedReservation = res;
        for (Customer c : cmbCustomer.getItems()) {
            if (c.getCustomerId() == res.getCustomerId()) {
                cmbCustomer.setValue(c);
                break;
            }
        }
        dpCheckIn.setValue(res.getCheckInDate());
        dpCheckOut.setValue(res.getCheckOutDate());
        lblCalculatedTotal.setText("$" + res.getTotalAmount());
        txtDepositPaid.setText(res.getPaidAmount() != null ? res.getPaidAmount().toString() : "0.00");
        cmbStatus.setValue(res.getStatus());
    }

    @FXML
    public void handleSave() {
        Customer cust = cmbCustomer.getValue();
        Room room = cmbRoom.getValue();
        LocalDate in = dpCheckIn.getValue();
        LocalDate out = dpCheckOut.getValue();
        String status = cmbStatus.getValue();
        String depositStr = txtDepositPaid.getText();

        if (cust == null) {
            AlertUtil.showWarning("Validation", "Customer Required", "Select a customer.");
            return;
        }
        if (room == null && selectedReservation == null) {
            AlertUtil.showWarning("Validation", "Room Required", "Select an available room.");
            return;
        }
        if (!ValidationUtil.isValidDateRange(in, out)) {
            AlertUtil.showWarning("Validation", "Invalid Dates", "Check-out date must be strictly after check-in date.");
            return;
        }

        BigDecimal deposit = BigDecimal.ZERO;
        if (ValidationUtil.isNotEmpty(depositStr)) {
            try {
                deposit = new BigDecimal(depositStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        try {
            BigDecimal totalCost = reservationService.calculateCost(
                    room != null ? room.getPricePerNight() : BigDecimal.ZERO, in, out);

            if (selectedReservation == null) {
                // New Booking
                Reservation res = new Reservation();
                res.setReservationCode(reservationService.generateUniqueReservationCode());
                res.setCustomerId(cust.getCustomerId());
                res.setRoomId(room.getRoomId());
                res.setCheckInDate(in);
                res.setCheckOutDate(out);
                res.setTotalAmount(totalCost);
                res.setPaidAmount(deposit);
                res.setStatus(status);
                if (AuthenticationService.getCurrentUser() != null) {
                    res.setCreatedBy(AuthenticationService.getCurrentUser().getUserId());
                }

                reservationService.createReservation(res);
                AlertUtil.showInfo("Booking Confirmed", "Reservation Created",
                        "Reservation #" + res.getReservationCode() + " successfully booked for " + cust.getName());
                handleClear();
                loadReservations();
            } else {
                // Update
                selectedReservation.setCustomerId(cust.getCustomerId());
                if (room != null) {
                    selectedReservation.setRoomId(room.getRoomId());
                }
                selectedReservation.setCheckInDate(in);
                selectedReservation.setCheckOutDate(out);
                selectedReservation.setTotalAmount(totalCost);
                selectedReservation.setPaidAmount(deposit);
                selectedReservation.setStatus(status);

                reservationDAO.update(selectedReservation);
                AlertUtil.showInfo("Updated", "Reservation Modified", "Reservation successfully updated.");
                handleClear();
                loadReservations();
            }
        } catch (Exception e) {
            AlertUtil.showError("Booking Error", "Operation Failed", e.getMessage());
        }
    }

    @FXML
    public void handleCancelReservation() {
        if (selectedReservation == null) {
            AlertUtil.showWarning("Selection Required", "No Reservation Selected", "Select a reservation to cancel.");
            return;
        }

        boolean confirm = AlertUtil.showConfirmation("Confirm Cancellation",
                "Cancel Reservation #" + selectedReservation.getReservationCode() + "?",
                "Are you sure you want to cancel this booking?");
        if (confirm) {
            try {
                reservationService.cancelReservation(selectedReservation.getReservationId());
                AlertUtil.showInfo("Cancelled", "Reservation Cancelled", "Booking has been cancelled.");
                handleClear();
                loadReservations();
            } catch (Exception e) {
                AlertUtil.showError("Cancel Error", "Cancellation Failed", e.getMessage());
            }
        }
    }

    @FXML
    public void handlePrintReceipt() {
        if (selectedReservation == null) {
            AlertUtil.showWarning("Selection Required", "No Reservation Selected", "Select a reservation from the table first.");
            return;
        }

        try {
            HotelSetting setting = settingDAO.getSettings();
            String receiptText = ReceiptGenerator.generateReservationReceipt(selectedReservation, setting);

            // Display in a readable dialog
            TextArea textArea = new TextArea(receiptText);
            textArea.setEditable(false);
            textArea.setWrapText(false);
            textArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Hotevia - Reservation Confirmation Receipt");
            dialog.setHeaderText("Official Confirmation Receipt: " + selectedReservation.getReservationCode());
            dialog.getDialogPane().setContent(textArea);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();

        } catch (Exception e) {
            AlertUtil.showError("Receipt Error", "Could not generate receipt", e.getMessage());
        }
    }

    @FXML
    public void handleClear() {
        selectedReservation = null;
        dpCheckIn.setValue(LocalDate.now());
        dpCheckOut.setValue(LocalDate.now().plusDays(2));
        txtDepositPaid.setText("0.00");
        cmbStatus.setValue("CONFIRMED");
        reservationTable.getSelectionModel().clearSelection();
        onDatesChanged();
    }
}
