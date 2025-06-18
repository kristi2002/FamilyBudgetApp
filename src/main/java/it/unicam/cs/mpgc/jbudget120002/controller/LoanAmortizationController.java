package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.Installment;
import it.unicam.cs.mpgc.jbudget120002.model.ScheduledTransaction;
import it.unicam.cs.mpgc.jbudget120002.service.LoanService;
import it.unicam.cs.mpgc.jbudget120002.service.ServiceFactory;
import it.unicam.cs.mpgc.jbudget120002.service.ScheduledTransactionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Collections;

public class LoanAmortizationController {
    @FXML private TextField tfAmount;
    @FXML private TextField tfInterestRate;
    @FXML private TextField tfTerm;
    @FXML private DatePicker dpStartDate;
    @FXML private Button btnGenerate;
    @FXML private TableView<Installment> tableSchedule;
    @FXML private TableColumn<Installment, Integer> colNumber;
    @FXML private TableColumn<Installment, LocalDate> colDueDate;
    @FXML private TableColumn<Installment, BigDecimal> colPrincipal;
    @FXML private TableColumn<Installment, BigDecimal> colInterest;
    @FXML private TableColumn<Installment, BigDecimal> colTotal;
    @FXML private Button btnSaveAsScheduled;
    @FXML private TextField tfLoanName;

    private final LoanService loanService = new LoanService();
    private final ObservableList<Installment> schedule = FXCollections.observableArrayList();
    private ServiceFactory serviceFactory;
    private ScheduledTransactionService scheduledTransactionService;

    @FXML
    public void initialize() {
        // Set up table columns
        colNumber.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getNumber()));
        colDueDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDueDate()));
        colPrincipal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPrincipal()));
        colInterest.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getInterest()));
        colTotal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalPayment()));

        // Format columns
        colDueDate.setCellFactory(column -> new TableCell<Installment, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : it.unicam.cs.mpgc.jbudget120002.util.DateTimeUtils.formatDate(date));
            }
        });
        colPrincipal.setCellFactory(column -> new TableCell<Installment, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%.2f", value));
            }
        });
        colInterest.setCellFactory(column -> new TableCell<Installment, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%.2f", value));
            }
        });
        colTotal.setCellFactory(column -> new TableCell<Installment, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%.2f", value));
            }
        });

        tableSchedule.setItems(schedule);

        btnGenerate.setOnAction(e -> generateSchedule());
        btnSaveAsScheduled.setOnAction(e -> showFutureFeatureAlert());
    }

    public void setServiceFactory(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
        this.scheduledTransactionService = serviceFactory.getScheduledTransactionService();
    }

    private void generateSchedule() {
        try {
            String loanName = tfLoanName.getText();
            if (loanName == null || loanName.trim().isEmpty()) loanName = "Loan";
            BigDecimal amount = new BigDecimal(tfAmount.getText());
            double rate = Double.parseDouble(tfInterestRate.getText());
            int term = Integer.parseInt(tfTerm.getText());
            LocalDate start = dpStartDate.getValue();
            List<Installment> result = loanService.generateAmortizationSchedule(amount, rate, term, start);
            schedule.setAll(result);
        } catch (Exception ex) {
            showError("Invalid input. Please check your values.");
        }
    }

    private void showFutureFeatureAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "This feature will be implemented in a future update.", ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Feature Coming Soon");
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
} 