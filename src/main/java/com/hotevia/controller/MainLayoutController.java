package com.hotevia.controller;

import com.hotevia.model.User;
import com.hotevia.service.AuthenticationService;
import com.hotevia.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainLayoutController {

    @FXML private Label userFullNameLabel;
    @FXML private Label userRoleBadge;
    @FXML private StackPane contentArea;

    @FXML private Button btnDashboard;
    @FXML private Button btnRooms;
    @FXML private Button btnCategories;
    @FXML private Button btnCustomers;
    @FXML private Button btnReservations;
    @FXML private Button btnCheckInOut;
    @FXML private Button btnPayments;
    @FXML private Button btnReports;
    @FXML private Button btnUsers;
    @FXML private Button btnSettings;

    private Button currentActiveBtn;
    private final Map<String, Parent> viewCache = new HashMap<>();

    @FXML
    public void initialize() {
        User user = AuthenticationService.getCurrentUser();
        if (user != null) {
            userFullNameLabel.setText(user.getFullName());
            userRoleBadge.setText(user.getRole());
            if (!user.isAdmin()) {
                // Receptionist cannot access Users management
                btnUsers.setVisible(false);
                btnUsers.setManaged(false);
            }
        }

        // Load Dashboard by default
        showDashboard();
    }

    @FXML
    public void showDashboard() {
        setActive(btnDashboard, "/views/dashboard.fxml");
    }

    @FXML
    public void showRooms() {
        setActive(btnRooms, "/views/rooms.fxml");
    }

    @FXML
    public void showCategories() {
        setActive(btnCategories, "/views/categories.fxml");
    }

    @FXML
    public void showCustomers() {
        setActive(btnCustomers, "/views/customers.fxml");
    }

    @FXML
    public void showReservations() {
        setActive(btnReservations, "/views/reservations.fxml");
    }

    @FXML
    public void showCheckInOut() {
        setActive(btnCheckInOut, "/views/checkinout.fxml");
    }

    @FXML
    public void showPayments() {
        setActive(btnPayments, "/views/payments.fxml");
    }

    @FXML
    public void showReports() {
        setActive(btnReports, "/views/reports.fxml");
    }

    @FXML
    public void showUsers() {
        if (!AuthenticationService.isAdmin()) {
            AlertUtil.showWarning("Access Denied", "Restricted Area", "Only administrators can access User Management.");
            return;
        }
        setActive(btnUsers, "/views/users.fxml");
    }

    @FXML
    public void showSettings() {
        setActive(btnSettings, "/views/settings.fxml");
    }

    private void setActive(Button button, String fxmlPath) {
        if (currentActiveBtn != null) {
            currentActiveBtn.getStyleClass().remove("sidebar-btn-active");
        }
        button.getStyleClass().add("sidebar-btn-active");
        currentActiveBtn = button;

        loadView(fxmlPath);
    }

    private void loadView(String fxmlPath) {
        try {
            // Fresh load ensures tables and statistics reflect current data
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "Could not load view", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout() {
        boolean confirm = AlertUtil.showConfirmation("Logout", "Confirm Logout", "Are you sure you want to sign out?");
        if (confirm) {
            AuthenticationService.logout();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) contentArea.getScene().getWindow();
                Scene scene = new Scene(root, 900, 600);
                scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
                stage.setTitle("Hotevia - Login");
                stage.setScene(scene);
                stage.centerOnScreen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
