package it.unicam.cs.mpgc.jbudget120002.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.time.YearMonth;
import it.unicam.cs.mpgc.jbudget120002.model.Deadline;
import it.unicam.cs.mpgc.jbudget120002.service.ServiceFactory;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Dialog;
import javafx.scene.layout.HBox;

public class DeadlinesCalendarController {
    @FXML private Button btnPrevMonth;
    @FXML private Button btnNextMonth;
    @FXML private Label lblMonthYear;
    @FXML private GridPane calendarGrid;

    private YearMonth currentMonth;
    private ServiceFactory serviceFactory;

    @FXML
    public void initialize() {
        currentMonth = YearMonth.now();
        updateCalendar();
        btnPrevMonth.setOnAction(e -> changeMonth(-1));
        btnNextMonth.setOnAction(e -> changeMonth(1));
    }

    public void setServiceFactory(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    private void changeMonth(int delta) {
        currentMonth = currentMonth.plusMonths(delta);
        updateCalendar();
    }

    private void updateCalendar() {
        lblMonthYear.setText(currentMonth.getMonth().toString() + " " + currentMonth.getYear());
        calendarGrid.getChildren().clear();

        // Add day of week headers
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < days.length; i++) {
            Label lbl = new Label(days[i]);
            lbl.setStyle("-fx-font-weight: bold; -fx-alignment: center;");
            calendarGrid.add(lbl, i, 0);
        }

        // Fetch all deadlines for the current month
        List<Deadline> allDeadlines = serviceFactory != null ? serviceFactory.getDeadlineService().findAll() : List.of();

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1=Mon ... 7=Sun
        int daysInMonth = currentMonth.lengthOfMonth();

        int col = (firstDayOfWeek - 1) % 7;
        int row = 1;
        for (int day = 1; day <= daysInMonth; day++) {
            StackPane dayCell = new StackPane();
            VBox cellBox = new VBox();
            cellBox.setSpacing(2);
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            cellBox.getChildren().add(dayLabel);

            LocalDate thisDate = currentMonth.atDay(day);
            List<Deadline> deadlinesForDay = allDeadlines.stream()
                .filter(d -> thisDate.equals(d.getDueDate()))
                .collect(Collectors.toList());
            if (!deadlinesForDay.isEmpty()) {
                Label deadlineLabel = new Label(deadlinesForDay.size() + (deadlinesForDay.size() == 1 ? " deadline" : " deadlines"));
                deadlineLabel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 4; -fx-background-radius: 4;");
                cellBox.getChildren().add(deadlineLabel);
                // Add click handler for popup
                dayCell.setOnMouseClicked(e -> {
                    Dialog<Void> dialog = new Dialog<>();
                    dialog.setTitle("Deadlines for " + thisDate);
                    VBox content = new VBox(10);
                    for (Deadline d : deadlinesForDay) {
                        VBox deadlineBox = new VBox(2);
                        deadlineBox.getChildren().add(new Label("Description: " + d.getDescription()));
                        deadlineBox.getChildren().add(new Label("Amount: " + d.getAmount()));
                        deadlineBox.getChildren().add(new Label("Category: " + d.getCategory()));
                        deadlineBox.getChildren().add(new Label("Status: " + (d.isPaid() ? "Paid" : "Unpaid")));
                        HBox actions = new HBox(5);
                        Button btnEdit = new Button("Edit");
                        btnEdit.setOnAction(ev -> {
                            System.out.println("Edit deadline: " + d.getDescription());
                            dialog.close();
                        });
                        Button btnDelete = new Button("Delete");
                        btnDelete.setOnAction(ev -> {
                            Alert confirm = new Alert(AlertType.CONFIRMATION, "Delete this deadline?", ButtonType.YES, ButtonType.NO);
                            confirm.setHeaderText(null);
                            confirm.showAndWait().ifPresent(type -> {
                                if (type == ButtonType.YES) {
                                    // Remove deadline from service and refresh
                                    if (serviceFactory != null) {
                                        serviceFactory.getDeadlineService().delete(d.getId());
                                    }
                                    dialog.close();
                                    updateCalendar();
                                }
                            });
                        });
                        actions.getChildren().addAll(btnEdit, btnDelete);
                        deadlineBox.getChildren().add(actions);
                        deadlineBox.setStyle("-fx-border-color: #ccc; -fx-padding: 5; -fx-background-radius: 4;");
                        content.getChildren().add(deadlineBox);
                    }
                    dialog.getDialogPane().setContent(content);
                    dialog.getDialogPane().getButtonTypes().add(new ButtonType("Close", ButtonData.CANCEL_CLOSE));
                    dialog.showAndWait();
                });
            }
            dayCell.getChildren().add(cellBox);
            calendarGrid.add(dayCell, col, row);
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }
} 