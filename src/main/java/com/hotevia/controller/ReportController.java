package com.hotevia.controller;

import com.hotevia.dao.PaymentDAO;
import com.hotevia.dao.ReservationDAO;
import com.hotevia.dao.RoomDAO;
import com.hotevia.model.Payment;
import com.hotevia.model.Reservation;
import com.hotevia.service.ReportService;
import com.hotevia.util.AlertUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;

public class ReportController {

    @FXML private Label lblOccupancyRate;
    @FXML private Label lblGrossRevenue;
    @FXML private Label lblTotalBookings;

    @FXML private BarChart<String, Number> monthlyRevenueChart;
    @FXML private CategoryAxis xAxisMonth;
    @FXML private NumberAxis yAxisRevenue;

    @FXML private PieChart paymentMethodChart;

    @FXML private TableView<Reservation> dailyReportTable;
    @FXML private TableColumn<Reservation, String> colResCode;
    @FXML private TableColumn<Reservation, String> colGuest;
    @FXML private TableColumn<Reservation, String> colRoom;
    @FXML private TableColumn<Reservation, BigDecimal> colTotal;
    @FXML private TableColumn<Reservation, String> colStatus;

    private final ReportService reportService = new ReportService();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    @FXML
    public void initialize() {
        colResCode.setCellValueFactory(new PropertyValueFactory<>("reservationCode"));
        colGuest.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadReports();
    }

    private void loadReports() {
        new Thread(() -> {
            try {
                double occRate = reportService.getOccupancyRate();
                BigDecimal totalRev = reservationDAO.getTotalRevenue();
                int totalRes = reservationDAO.countTotalReservations();

                int currentYear = Year.now().getValue();
                Map<String, BigDecimal> monthly = reportService.getMonthlyRevenueForYear(currentYear);
                Map<String, BigDecimal> methods = reportService.getRevenueByPaymentMethod();
                List<Reservation> allRes = reservationDAO.findAll();

                Platform.runLater(() -> {
                    lblOccupancyRate.setText(String.format("%.1f%%", occRate));
                    lblGrossRevenue.setText(String.format("$%,.2f", totalRev));
                    lblTotalBookings.setText(String.valueOf(totalRes));

                    // Monthly Bar Chart
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Revenue (" + currentYear + ")");
                    monthly.forEach((m, amt) -> series.getData().add(new XYChart.Data<>(m, amt)));
                    monthlyRevenueChart.getData().clear();
                    monthlyRevenueChart.getData().add(series);

                    // Payment Method Pie Chart
                    ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                    methods.forEach((meth, amt) -> {
                        if (amt.compareTo(BigDecimal.ZERO) > 0) {
                            pieData.add(new PieChart.Data(meth + " ($" + amt + ")", amt.doubleValue()));
                        }
                    });
                    paymentMethodChart.setData(pieData);

                    // Table
                    dailyReportTable.setItems(FXCollections.observableArrayList(allRes));
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Error", "Could not generate reports", e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void handlePrintSummary() {
        try {
            double occRate = reportService.getOccupancyRate();
            BigDecimal totalRev = reservationDAO.getTotalRevenue();
            int totalRes = reservationDAO.countTotalReservations();
            int totalRooms = roomDAO.countTotalRooms();
            int availableRooms = roomDAO.countRoomsByStatus("AVAILABLE");
            int occupiedRooms = roomDAO.countRoomsByStatus("OCCUPIED");

            StringBuilder sb = new StringBuilder();
            sb.append("=========================================================\n");
            sb.append("         HOTEVIA EXECUTIVE MANAGEMENT REPORT             \n");
            sb.append("         Date: ").append(LocalDate.now()).append("\n");
            sb.append("=========================================================\n\n");
            sb.append(String.format("%-30s: %d\n", "Total Hotel Rooms", totalRooms));
            sb.append(String.format("%-30s: %d\n", "Available Rooms", availableRooms));
            sb.append(String.format("%-30s: %d\n", "Occupied Rooms", occupiedRooms));
            sb.append(String.format("%-30s: %.2f%%\n", "Current Occupancy Rate", occRate));
            sb.append(String.format("%-30s: %d\n", "Total Cumulative Bookings", totalRes));
            sb.append(String.format("%-30s: $%,.2f\n", "Total Revenue Collected", totalRev));
            sb.append("\n=========================================================\n");
            sb.append("Generated by Hotevia Analytics Reporting Subsystem\n");
            sb.append("=========================================================\n");

            TextArea ta = new TextArea(sb.toString());
            ta.setEditable(false);
            ta.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 13px;");

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Hotevia - Executive Report");
            dialog.setHeaderText("Hotel Operational & Financial Overview");
            dialog.getDialogPane().setContent(ta);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();

        } catch (Exception e) {
            AlertUtil.showError("Report Error", "Failed to compile summary report", e.getMessage());
        }
    }
}
