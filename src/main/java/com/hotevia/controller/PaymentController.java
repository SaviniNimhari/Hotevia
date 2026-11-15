package com.hotevia.controller;

import com.hotevia.dao.PaymentDAO;
import com.hotevia.dao.ReservationDAO;
import com.hotevia.model.Payment;
import com.hotevia.model.Reservation;
import com.hotevia.service.PaymentService;
import com.hotevia.util.AlertUtil;
import com.hotevia.util.ValidationUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentController {

    @FXML private TableView<Payment> paymentTable;
    @FXML private TableColumn<Payment, Integer> colPayId;
    @FXML private TableColumn<Payment, String> colResCode;
    @FXML private TableColumn<Payment, String> colCustomer;
    @FXML private TableColumn<Payment, BigDecimal> colAmount;
    @FXML private TableColumn<Payment, String> colMethod;
    @FXML private TableColumn<Payment, LocalDateTime> colDate;
    @FXML private TableColumn<Payment, String> colNotes;

    @FXML private ComboBox<Reservation> cmbReservation;
    @FXML private Label lblTotalDue;
    @FXML private Label lblAlreadyPaid;
    @FXML private Label lblBalance;

    @FXML private TextField txtAmount;
    @FXML private ComboBox<String> cmbMethod;
    @FXML private TextField txtNotes;

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final PaymentService paymentService = new PaymentService();

    private final ObservableList<Payment> paymentList = FXCollections.observableArrayList();
    private Payment selectedPayment = null;

    @FXML
    public void initialize() {
        colPayId.setCellValueFactory(new PropertyValueFactory<>("paymentId"));
        colResCode.setCellValueFactory(new PropertyValueFactory<>("reservationCode"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        cmbMethod.setItems(FXCollections.observableArrayList("CASH", "CREDIT_CARD", "DEBIT_CARD", "ONLINE_TRANSFER"));
        cmbMethod.setValue("CREDIT_CARD");

        cmbReservation.valueProperty().addListener((obs, o, n) -> {
            if (n != null) {
                lblTotalDue.setText("$" + n.getTotalAmount());
                lblAlreadyPaid.setText("$" + (n.getPaidAmount() != null ? n.getPaidAmount() : "0.00"));
                lblBalance.setText("$" + n.getBalanceAmount());
                txtAmount.setText(n.getBalanceAmount().toString());
            } else {
                lblTotalDue.setText("$0.00");
                lblAlreadyPaid.setText("$0.00");
                lblBalance.setText("$0.00");
                txtAmount.clear();
            }
        });

        paymentTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            selectedPayment = n;
        });

        loadReservations();
        loadPayments();
    }

    private void loadReservations() {
        try {
            List<Reservation> list = reservationDAO.findAll();
            // Show reservations with remaining balance or active
            cmbReservation.setItems(FXCollections.observableArrayList(list));
            if (!list.isEmpty()) {
                cmbReservation.setValue(list.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPayments() {
        new Thread(() -> {
            try {
                List<Payment> list = paymentDAO.findAll();
                Platform.runLater(() -> {
                    paymentList.setAll(list);
                    paymentTable.setItems(paymentList);
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Error", "Could not load payments", e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void handleProcessPayment() {
        Reservation res = cmbReservation.getValue();
        String amountStr = txtAmount.getText();
        String method = cmbMethod.getValue();
        String notes = txtNotes.getText();

        if (res == null) {
            AlertUtil.showWarning("Validation", "Reservation Required", "Please select a reservation.");
            return;
        }
        if (!ValidationUtil.isPositiveDecimal(amountStr)) {
            AlertUtil.showWarning("Validation", "Invalid Amount", "Please enter a valid positive payment amount.");
            return;
        }

        BigDecimal amount = new BigDecimal(amountStr.trim());

        try {
            Payment payment = new Payment();
            payment.setReservationId(res.getReservationId());
            payment.setAmount(amount);
            payment.setPaymentMethod(method);
            payment.setNotes(notes != null ? notes.trim() : "Front Desk Payment");

            paymentService.processPayment(payment);
            AlertUtil.showInfo("Payment Successful", "Transaction Recorded",
                    "Payment of $" + amount + " processed successfully via " + method);

            txtAmount.clear();
            txtNotes.clear();
            loadReservations();
            loadPayments();
        } catch (Exception e) {
            AlertUtil.showError("Payment Failed", "Transaction Rejected", e.getMessage());
        }
    }

    @FXML
    public void handlePrintPaymentReceipt() {
        Payment p = (selectedPayment != null) ? selectedPayment : 
                (!paymentList.isEmpty() ? paymentList.get(0) : null);

        if (p == null) {
            AlertUtil.showWarning("Selection Required", "No Payment Selected", "Select a payment transaction from the table.");
            return;
        }

        try {
            String receipt = paymentService.generateReceipt(p);

            TextArea textArea = new TextArea(receipt);
            textArea.setEditable(false);
            textArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Hotevia - Official Payment Receipt");
            dialog.setHeaderText("Payment Receipt: PAY-" + String.format("%06d", p.getPaymentId()));
            dialog.getDialogPane().setContent(textArea);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();

        } catch (Exception e) {
            AlertUtil.showError("Receipt Error", "Could not generate payment receipt", e.getMessage());
        }
    }
}
