package com.hotevia.controller;

import com.hotevia.Main;
import com.hotevia.service.AuthenticationService;
import com.hotevia.util.AlertUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator progressIndicator;

    private final AuthenticationService authService = new AuthenticationService();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        progressIndicator.setVisible(false);
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        progressIndicator.setVisible(true);
        errorLabel.setVisible(false);

        new Thread(() -> {
            try {
                var user = authService.authenticate(username, password);
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    if (user != null) {
                        loadMainApplication();
                    } else {
                        showError("Invalid username or password.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    showError("Login error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    private void loadMainApplication() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main_layout.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 780);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

            stage.setTitle("Hotevia - Smart Hotel Reservation & Management System");
            stage.setScene(scene);
            stage.setMinWidth(1100);
            stage.setMinHeight(700);
            stage.centerOnScreen();
        } catch (Exception e) {
            AlertUtil.showError("Application Error", "Failed to launch main dashboard", e.getMessage());
            e.printStackTrace();
        }
    }
}
