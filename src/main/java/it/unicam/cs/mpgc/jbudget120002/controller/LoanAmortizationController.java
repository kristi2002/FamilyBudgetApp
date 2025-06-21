package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.Installment;
import it.unicam.cs.mpgc.jbudget120002.model.User;
import it.unicam.cs.mpgc.jbudget120002.service.LoanService;
import it.unicam.cs.mpgc.jbudget120002.service.ScheduledTransactionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class LoanAmortizationController extends BaseController {
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
    @FXML
    private TextField principalField;

    private LoanService loanService;
    private final ObservableList<Installment> schedule = FXCollections.observableArrayList();
    private ScheduledTransactionService scheduledTransactionService;
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @Override
    protected void initializeServices() {
        this.scheduledTransactionService = serviceFactory.getScheduledTransactionService(false);
    }

    @Override
    protected void setupUI() {
        // Set up table columns
        colNumber.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getNumber()));
        colDueDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDueDate()));
        colPrincipal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPrincipal()));
        colInterest.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getInterest()));
        colTotal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalPayment()));

        // Format columns
        colDueDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : it.unicam.cs.mpgc.jbudget120002.util.DateTimeUtils.formatDate(date));
            }
        });
        colPrincipal.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%.2f", value));
            }
        });
        colInterest.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%.2f", value));
            }
        });
        colTotal.setCellFactory(column -> new TableCell<>() {
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

    @Override
    protected void loadData() {
        // Nothing to load initially
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
            showError("Invalid Input", "Please check your values.");
        }
    }

    private void showFutureFeatureAlert() {
        showInfo("Feature Coming Soon", "This feature will be implemented in a future update.");
    }
} 