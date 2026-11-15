package com.hotevia.controller;

import com.hotevia.dao.HotelSettingDAO;
import com.hotevia.dao.ReservationDAO;
import com.hotevia.dao.RoomDAO;
import com.hotevia.model.HotelSetting;
import com.hotevia.model.Reservation;
import com.hotevia.service.ReportService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DashboardController {

    @FXML private Label lblTotalRooms;
    @FXML private Label lblAvailableRooms;
    @FXML private Label lblOccupiedRooms;
    @FXML private Label lblTotalReservations;
    @FXML private Label lblTodayCheckIns;
    @FXML private Label lblTodayCheckOuts;
    @FXML private Label lblTotalRevenue;

    @FXML private PieChart occupancyPieChart;

    @FXML private TableView<Reservation> recentReservationsTable;
    @FXML private TableColumn<Reservation, String> colCode;
    @FXML private TableColumn<Reservation, String> colCustomer;
    @FXML private TableColumn<Reservation, String> colRoom;
    @FXML private TableColumn<Reservation, LocalDate> colCheckIn;
    @FXML private TableColumn<Reservation, LocalDate> colCheckOut;
    @FXML private TableColumn<Reservation, BigDecimal> colTotal;
    @FXML private TableColumn<Reservation, String> colStatus;

    private final RoomDAO roomDAO = new RoomDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final ReportService reportService = new ReportService();
    private final HotelSettingDAO settingDAO = new HotelSettingDAO();

    @FXML
    public void initialize() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("reservationCode"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadDashboardMetrics();
    }

    private void loadDashboardMetrics() {
        new Thread(() -> {
            try {
                int totalRooms = roomDAO.countTotalRooms();
                int availableRooms = roomDAO.countRoomsByStatus("AVAILABLE");
                int occupiedRooms = roomDAO.countRoomsByStatus("OCCUPIED");
                int totalRes = reservationDAO.countTotalReservations();
                int todayIn = reservationDAO.countTodayCheckIns();
                int todayOut = reservationDAO.countTodayCheckOuts();
                BigDecimal revenue = reservationDAO.getTotalRevenue();
                HotelSetting setting = settingDAO.getSettings();
                String cur = setting.getCurrency() != null ? setting.getCurrency() : "$";

                Map<String, Integer> statusMap = reportService.getRoomStatusCounts();
                List<Reservation> allReservations = reservationDAO.findAll();
                List<Reservation> recent = allReservations.stream().limit(8).toList();

                Platform.runLater(() -> {
                    lblTotalRooms.setText(String.valueOf(totalRooms));
                    lblAvailableRooms.setText(String.valueOf(availableRooms));
                    lblOccupiedRooms.setText(String.valueOf(occupiedRooms));
                    lblTotalReservations.setText(String.valueOf(totalRes));
                    lblTodayCheckIns.setText(String.valueOf(todayIn));
                    lblTodayCheckOuts.setText(String.valueOf(todayOut));
                    lblTotalRevenue.setText(cur + " " + String.format("%,.2f", revenue));

                    // Populate PieChart
                    ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                    statusMap.forEach((status, count) -> {
                        if (count > 0) {
                            pieData.add(new PieChart.Data(status + " (" + count + ")", count));
                        }
                    });
                    occupancyPieChart.setData(pieData);

                    // Populate Table
                    recentReservationsTable.setItems(FXCollections.observableArrayList(recent));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
