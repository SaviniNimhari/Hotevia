package com.hotevia.controller;

import com.hotevia.dao.CustomerDAO;
import com.hotevia.dao.ReservationDAO;
import com.hotevia.model.Customer;
import com.hotevia.model.Reservation;
import com.hotevia.util.AlertUtil;
import com.hotevia.util.ValidationUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class CustomerController {

    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colPassport;
    @FXML private TableColumn<Customer, String> colAddress;

    @FXML private TextField txtSearch;
    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextField txtPassport;
    @FXML private TextArea txtAddress;

    @FXML private TableView<Reservation> historyTable;
    @FXML private TableColumn<Reservation, String> colHistCode;
    @FXML private TableColumn<Reservation, String> colHistRoom;
    @FXML private TableColumn<Reservation, String> colHistDates;
    @FXML private TableColumn<Reservation, String> colHistStatus;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private Customer selectedCustomer = null;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colPassport.setCellValueFactory(new PropertyValueFactory<>("idCardOrPassport"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

        colHistCode.setCellValueFactory(new PropertyValueFactory<>("reservationCode"));
        colHistRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colHistDates.setCellValueFactory(cellData -> {
            Reservation r = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    (r.getCheckInDate() != null ? r.getCheckInDate().toString() : "") + " to " +
                    (r.getCheckOutDate() != null ? r.getCheckOutDate().toString() : ""));
        });
        colHistStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
                loadCustomerHistory(newVal.getCustomerId());
            }
        });

        loadCustomers();
    }

    private void loadCustomers() {
        new Thread(() -> {
            try {
                List<Customer> list = customerDAO.findAll();
                Platform.runLater(() -> {
                    customerList.setAll(list);
                    customerTable.setItems(customerList);
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Data Error", "Could not load customers", e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void handleSearch() {
        String query = txtSearch.getText();
        if (query == null || query.trim().isEmpty()) {
            loadCustomers();
            return;
        }

        new Thread(() -> {
            try {
                List<Customer> results = customerDAO.search(query.trim());
                Platform.runLater(() -> customerTable.setItems(FXCollections.observableArrayList(results)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void populateForm(Customer c) {
        selectedCustomer = c;
        txtName.setText(c.getName());
        txtEmail.setText(c.getEmail());
        txtPhone.setText(c.getPhone());
        txtPassport.setText(c.getIdCardOrPassport());
        txtAddress.setText(c.getAddress());
    }

    private void loadCustomerHistory(int customerId) {
        new Thread(() -> {
            try {
                List<Reservation> all = reservationDAO.findAll();
                List<Reservation> customerRes = all.stream()
                        .filter(r -> r.getCustomerId() == customerId)
                        .toList();
                Platform.runLater(() -> historyTable.setItems(FXCollections.observableArrayList(customerRes)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    public void handleSave() {
        String name = txtName.getText();
        String email = txtEmail.getText();
        String phone = txtPhone.getText();
        String passport = txtPassport.getText();
        String address = txtAddress.getText();

        if (!ValidationUtil.isNotEmpty(name)) {
            AlertUtil.showWarning("Validation", "Name Required", "Guest full name is required.");
            return;
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            AlertUtil.showWarning("Validation", "Invalid Phone", "Please provide a valid contact phone number.");
            return;
        }
        if (ValidationUtil.isNotEmpty(email) && !ValidationUtil.isValidEmail(email)) {
            AlertUtil.showWarning("Validation", "Invalid Email", "Please enter a valid email address.");
            return;
        }

        try {
            if (selectedCustomer == null) {
                Customer c = new Customer(name.trim(), email != null ? email.trim() : "", phone.trim(),
                        address != null ? address.trim() : "", passport != null ? passport.trim() : "");
                boolean ok = customerDAO.insert(c);
                if (ok) {
                    AlertUtil.showInfo("Saved", "Customer Registered", "Guest profile created successfully.");
                    handleClear();
                    loadCustomers();
                }
            } else {
                selectedCustomer.setName(name.trim());
                selectedCustomer.setEmail(email != null ? email.trim() : "");
                selectedCustomer.setPhone(phone.trim());
                selectedCustomer.setAddress(address != null ? address.trim() : "");
                selectedCustomer.setIdCardOrPassport(passport != null ? passport.trim() : "");
                boolean ok = customerDAO.update(selectedCustomer);
                if (ok) {
                    AlertUtil.showInfo("Saved", "Customer Updated", "Guest profile updated successfully.");
                    handleClear();
                    loadCustomers();
                }
            }
        } catch (Exception e) {
            AlertUtil.showError("Save Error", "Could not save customer", e.getMessage());
        }
    }

    @FXML
    public void handleDelete() {
        if (selectedCustomer == null) {
            AlertUtil.showWarning("Selection Required", "No Customer Selected", "Select a guest from the table.");
            return;
        }

        boolean confirm = AlertUtil.showConfirmation("Confirm Delete", "Delete " + selectedCustomer.getName() + "?",
                "Ensure the customer has no active reservations before deleting.");
        if (confirm) {
            try {
                boolean ok = customerDAO.delete(selectedCustomer.getCustomerId());
                if (ok) {
                    AlertUtil.showInfo("Deleted", "Customer Removed", "Customer deleted successfully.");
                    handleClear();
                    loadCustomers();
                }
            } catch (Exception e) {
                AlertUtil.showError("Delete Error", "Cannot delete customer", "Customer has existing reservations.");
            }
        }
    }

    @FXML
    public void handleClear() {
        selectedCustomer = null;
        txtName.clear();
        txtEmail.clear();
        txtPhone.clear();
        txtPassport.clear();
        txtAddress.clear();
        historyTable.setItems(FXCollections.emptyObservableList());
        customerTable.getSelectionModel().clearSelection();
    }
}
