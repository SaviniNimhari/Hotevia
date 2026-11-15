package com.hotevia.controller;

import com.hotevia.dao.UserDAO;
import com.hotevia.model.User;
import com.hotevia.util.AlertUtil;
import com.hotevia.util.PasswordUtil;
import com.hotevia.util.ValidationUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.List;

public class UserController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, LocalDateTime> colCreatedAt;

    @FXML private TextField txtUsername;
    @FXML private TextField txtFullName;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbRole;
    @FXML private ComboBox<String> cmbStatus;

    private final UserDAO userDAO = new UserDAO();
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private User selectedUser = null;

    @FXML
    public void initialize() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        cmbRole.setItems(FXCollections.observableArrayList("ADMIN", "RECEPTIONIST"));
        cmbRole.setValue("RECEPTIONIST");

        cmbStatus.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));
        cmbStatus.setValue("ACTIVE");

        userTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                populateForm(n);
            }
        });

        loadUsers();
    }

    private void loadUsers() {
        new Thread(() -> {
            try {
                List<User> list = userDAO.findAll();
                Platform.runLater(() -> {
                    userList.setAll(list);
                    userTable.setItems(userList);
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Data Error", "Could not load users", e.getMessage()));
            }
        }).start();
    }

    private void populateForm(User user) {
        selectedUser = user;
        txtUsername.setText(user.getUsername());
        txtUsername.setDisable(true); // Don't allow changing existing username
        txtFullName.setText(user.getFullName());
        txtPassword.clear();
        cmbRole.setValue(user.getRole());
        cmbStatus.setValue(user.getStatus());
    }

    @FXML
    public void handleSave() {
        String username = txtUsername.getText();
        String fullName = txtFullName.getText();
        String rawPassword = txtPassword.getText();
        String role = cmbRole.getValue();
        String status = cmbStatus.getValue();

        if (!ValidationUtil.isNotEmpty(username)) {
            AlertUtil.showWarning("Validation", "Username Required", "Please enter a valid username.");
            return;
        }
        if (!ValidationUtil.isNotEmpty(fullName)) {
            AlertUtil.showWarning("Validation", "Full Name Required", "Please enter the staff member's full name.");
            return;
        }

        try {
            if (selectedUser == null) {
                // New User requires password
                if (!ValidationUtil.isNotEmpty(rawPassword)) {
                    AlertUtil.showWarning("Validation", "Password Required", "Please enter a password for the new account.");
                    return;
                }
                String passHash = PasswordUtil.hashPassword(rawPassword.trim());
                User u = new User(username.trim(), passHash, fullName.trim(), role);
                u.setStatus(status);

                boolean ok = userDAO.insert(u);
                if (ok) {
                    AlertUtil.showInfo("Created", "User Account Created", "Account @" + username + " created successfully.");
                    handleClear();
                    loadUsers();
                }
            } else {
                // Update
                selectedUser.setFullName(fullName.trim());
                selectedUser.setRole(role);
                selectedUser.setStatus(status);

                boolean ok = userDAO.update(selectedUser);

                // If password was also typed, update password too
                if (ValidationUtil.isNotEmpty(rawPassword)) {
                    userDAO.updatePassword(selectedUser.getUserId(), PasswordUtil.hashPassword(rawPassword.trim()));
                }

                if (ok) {
                    AlertUtil.showInfo("Updated", "User Account Updated", "Account @" + selectedUser.getUsername() + " updated.");
                    handleClear();
                    loadUsers();
                }
            }
        } catch (Exception e) {
            AlertUtil.showError("Save Error", "Could not save user", e.getMessage());
        }
    }

    @FXML
    public void handleDelete() {
        if (selectedUser == null) {
            AlertUtil.showWarning("Selection Required", "No User Selected", "Select a staff member from the table.");
            return;
        }

        if ("admin".equalsIgnoreCase(selectedUser.getUsername())) {
            AlertUtil.showError("Protected Account", "Cannot Delete", "The primary system administrator cannot be deleted.");
            return;
        }

        boolean confirm = AlertUtil.showConfirmation("Confirm Deletion", "Delete account @" + selectedUser.getUsername() + "?",
                "Are you sure you want to delete this user?");
        if (confirm) {
            try {
                boolean ok = userDAO.delete(selectedUser.getUserId());
                if (ok) {
                    AlertUtil.showInfo("Deleted", "Account Deleted", "Staff user removed successfully.");
                    handleClear();
                    loadUsers();
                }
            } catch (Exception e) {
                AlertUtil.showError("Delete Error", "Cannot delete user", e.getMessage());
            }
        }
    }

    @FXML
    public void handleClear() {
        selectedUser = null;
        txtUsername.setDisable(false);
        txtUsername.clear();
        txtFullName.clear();
        txtPassword.clear();
        cmbRole.setValue("RECEPTIONIST");
        cmbStatus.setValue("ACTIVE");
        userTable.getSelectionModel().clearSelection();
    }
}
