package com.hotevia.controller;

import com.hotevia.dao.HotelSettingDAO;
import com.hotevia.model.HotelSetting;
import com.hotevia.model.User;
import com.hotevia.service.AuthenticationService;
import com.hotevia.util.AlertUtil;
import com.hotevia.util.ValidationUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.math.BigDecimal;

public class SettingsController {

    @FXML private TextField txtHotelName;
    @FXML private TextField txtAddress;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtCurrency;
    @FXML private TextField txtTaxRate;
    @FXML private TextField txtServiceCharge;

    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;

    private final HotelSettingDAO settingDAO = new HotelSettingDAO();
    private final AuthenticationService authService = new AuthenticationService();
    private HotelSetting currentSettings;

    @FXML
    public void initialize() {
        loadSettings();
    }

    private void loadSettings() {
        new Thread(() -> {
            try {
                currentSettings = settingDAO.getSettings();
                Platform.runLater(() -> {
                    if (currentSettings != null) {
                        txtHotelName.setText(currentSettings.getHotelName());
                        txtAddress.setText(currentSettings.getAddress());
                        txtPhone.setText(currentSettings.getContactNumber());
                        txtEmail.setText(currentSettings.getEmail());
                        txtCurrency.setText(currentSettings.getCurrency());
                        txtTaxRate.setText(currentSettings.getTaxRate().toString());
                        txtServiceCharge.setText(currentSettings.getServiceChargeRate().toString());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Error", "Could not load settings", e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void handleSaveHotelSettings() {
        if (!AuthenticationService.isAdmin()) {
            AlertUtil.showWarning("Access Restricted", "Admin Only", "Only administrators can modify hotel property parameters.");
            return;
        }

        String name = txtHotelName.getText();
        String addr = txtAddress.getText();
        String phone = txtPhone.getText();
        String email = txtEmail.getText();
        String cur = txtCurrency.getText();
        String taxStr = txtTaxRate.getText();
        String svcStr = txtServiceCharge.getText();

        if (!ValidationUtil.isNotEmpty(name)) {
            AlertUtil.showWarning("Validation", "Name Required", "Hotel name cannot be empty.");
            return;
        }

        BigDecimal tax = new BigDecimal("10.00");
        BigDecimal svc = new BigDecimal("5.00");
        try {
            if (ValidationUtil.isNotEmpty(taxStr)) tax = new BigDecimal(taxStr.trim());
            if (ValidationUtil.isNotEmpty(svcStr)) svc = new BigDecimal(svcStr.trim());
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validation", "Invalid Rates", "Tax and service charges must be valid numbers.");
            return;
        }

        try {
            currentSettings.setHotelName(name.trim());
            currentSettings.setAddress(addr != null ? addr.trim() : "");
            currentSettings.setContactNumber(phone != null ? phone.trim() : "");
            currentSettings.setEmail(email != null ? email.trim() : "");
            currentSettings.setCurrency(cur != null && !cur.isEmpty() ? cur.trim() : "$");
            currentSettings.setTaxRate(tax);
            currentSettings.setServiceChargeRate(svc);

            boolean ok = settingDAO.updateSettings(currentSettings);
            if (ok) {
                AlertUtil.showInfo("Saved", "Settings Updated", "Hotel configuration saved successfully.");
            }
        } catch (Exception e) {
            AlertUtil.showError("Save Error", "Could not update settings", e.getMessage());
        }
    }

    @FXML
    public void handleChangePassword() {
        String currentPass = txtCurrentPassword.getText();
        String newPass = txtNewPassword.getText();
        String confirmPass = txtConfirmPassword.getText();

        if (!ValidationUtil.isNotEmpty(currentPass) || !ValidationUtil.isNotEmpty(newPass)) {
            AlertUtil.showWarning("Validation", "Password Required", "Please fill in all password fields.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            AlertUtil.showWarning("Validation", "Mismatch", "New password and confirmation do not match.");
            return;
        }

        User user = AuthenticationService.getCurrentUser();
        if (user == null) return;

        try {
            boolean ok = authService.changePassword(user.getUserId(), currentPass, newPass);
            if (ok) {
                AlertUtil.showInfo("Success", "Password Changed", "Your password has been successfully updated.");
                txtCurrentPassword.clear();
                txtNewPassword.clear();
                txtConfirmPassword.clear();
            } else {
                AlertUtil.showError("Failed", "Incorrect Password", "The current password entered is incorrect.");
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", "Password update error", e.getMessage());
        }
    }
}
