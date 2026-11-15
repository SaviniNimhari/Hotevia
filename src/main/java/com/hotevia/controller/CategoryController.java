package com.hotevia.controller;

import com.hotevia.dao.RoomCategoryDAO;
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

public class CategoryController {

    @FXML private TableView<RoomCategory> categoryTable;
    @FXML private TableColumn<RoomCategory, String> colName;
    @FXML private TableColumn<RoomCategory, BigDecimal> colPrice;
    @FXML private TableColumn<RoomCategory, Integer> colCapacity;
    @FXML private TableColumn<RoomCategory, String> colDescription;

    @FXML private TextField txtName;
    @FXML private TextField txtPrice;
    @FXML private TextField txtCapacity;
    @FXML private TextArea txtDescription;

    private final RoomCategoryDAO categoryDAO = new RoomCategoryDAO();
    private final ObservableList<RoomCategory> categoryList = FXCollections.observableArrayList();
    private RoomCategory selectedCategory = null;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        categoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });

        loadCategories();
    }

    private void loadCategories() {
        new Thread(() -> {
            try {
                List<RoomCategory> list = categoryDAO.findAll();
                Platform.runLater(() -> {
                    categoryList.setAll(list);
                    categoryTable.setItems(categoryList);
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Error", "Could not load categories", e.getMessage()));
            }
        }).start();
    }

    private void populateForm(RoomCategory cat) {
        selectedCategory = cat;
        txtName.setText(cat.getCategoryName());
        txtPrice.setText(cat.getPricePerNight() != null ? cat.getPricePerNight().toString() : "");
        txtCapacity.setText(String.valueOf(cat.getCapacity()));
        txtDescription.setText(cat.getDescription());
    }

    @FXML
    public void handleSave() {
        String name = txtName.getText();
        String priceStr = txtPrice.getText();
        String capStr = txtCapacity.getText();
        String desc = txtDescription.getText();

        if (!ValidationUtil.isNotEmpty(name)) {
            AlertUtil.showWarning("Validation", "Name Required", "Category name cannot be empty.");
            return;
        }
        if (!ValidationUtil.isPositiveDecimal(priceStr)) {
            AlertUtil.showWarning("Validation", "Invalid Price", "Please enter a valid price per night (e.g. 150.00).");
            return;
        }
        if (!ValidationUtil.isPositiveInteger(capStr)) {
            AlertUtil.showWarning("Validation", "Invalid Capacity", "Please enter a positive guest capacity (e.g. 2).");
            return;
        }

        BigDecimal price = new BigDecimal(priceStr.trim());
        int capacity = Integer.parseInt(capStr.trim());

        try {
            if (selectedCategory == null) {
                RoomCategory cat = new RoomCategory(name.trim(), desc != null ? desc.trim() : "", price, capacity);
                boolean ok = categoryDAO.insert(cat);
                if (ok) {
                    AlertUtil.showInfo("Saved", "Category Created", "Room category created successfully.");
                    handleClear();
                    loadCategories();
                }
            } else {
                selectedCategory.setCategoryName(name.trim());
                selectedCategory.setPricePerNight(price);
                selectedCategory.setCapacity(capacity);
                selectedCategory.setDescription(desc != null ? desc.trim() : "");
                boolean ok = categoryDAO.update(selectedCategory);
                if (ok) {
                    AlertUtil.showInfo("Saved", "Category Updated", "Room category updated successfully.");
                    handleClear();
                    loadCategories();
                }
            }
        } catch (Exception e) {
            AlertUtil.showError("Save Error", "Could not save category", e.getMessage());
        }
    }

    @FXML
    public void handleDelete() {
        if (selectedCategory == null) {
            AlertUtil.showWarning("Selection Required", "No Category Selected", "Please select a category to delete.");
            return;
        }

        boolean confirm = AlertUtil.showConfirmation("Confirm Delete", "Delete " + selectedCategory.getCategoryName() + "?",
                "Ensure no existing rooms are assigned to this category before deleting.");
        if (confirm) {
            try {
                boolean ok = categoryDAO.delete(selectedCategory.getCategoryId());
                if (ok) {
                    AlertUtil.showInfo("Deleted", "Category Removed", "Room category deleted successfully.");
                    handleClear();
                    loadCategories();
                }
            } catch (Exception e) {
                AlertUtil.showError("Delete Error", "Cannot delete category", "Category is currently in use by hotel rooms.");
            }
        }
    }

    @FXML
    public void handleClear() {
        selectedCategory = null;
        txtName.clear();
        txtPrice.clear();
        txtCapacity.clear();
        txtDescription.clear();
        categoryTable.getSelectionModel().clearSelection();
    }
}
