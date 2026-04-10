package com.kairos.controller;

import com.kairos.dao.EntryDAO;
import com.kairos.model.Entry;
import com.kairos.model.Entry.EntryType;
import com.kairos.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Reminders TableView ({@code reminder-view.fxml}).
 *
 * <p>Rows are colour-coded:
 * <ul>
 *   <li><b>RED</b>    — reminder time is in the past (overdue)</li>
 *   <li><b>YELLOW</b> — reminder time is today</li>
 *   <li><b>GREEN</b>  — reminder time is in the future</li>
 * </ul>
 *
 * @author Kairos
 * @version 1.0.0
 */
public class ReminderController implements Initializable {

    // ─────────────────────────────────────────────────────────────────────────
    // FXML Injections
    // ─────────────────────────────────────────────────────────────────────────

    @FXML private TableView<Entry>           remindersTable;
    @FXML private TableColumn<Entry, String> colTitle;
    @FXML private TableColumn<Entry, String> colReminderTime;
    @FXML private TableColumn<Entry, String> colPriority;
    @FXML private TableColumn<Entry, String> colStatus;
    @FXML private TableColumn<Entry, Void>   colActions;

    // ─────────────────────────────────────────────────────────────────────────
    // State
    // ─────────────────────────────────────────────────────────────────────────

    private final EntryDAO entryDAO = new EntryDAO();
    private DashboardController dashboardController;

    // ─────────────────────────────────────────────────────────────────────────
    // Initialise
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        loadReminders();
    }

    /** Injects parent dashboard reference for post-CRUD refreshes. */
    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Table Setup
    // ─────────────────────────────────────────────────────────────────────────

    private void setupColumns() {
        colTitle       .setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTitle()));
        colReminderTime.setCellValueFactory(cd -> new SimpleStringProperty(
                DateUtil.formatDisplay(cd.getValue().getReminderTime())));
        colPriority    .setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getPriority().name()));
        colStatus      .setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().isCompleted() ? "✔ Done" : "⏰ Active"));

        // Priority styling
        colPriority.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                getStyleClass().removeAll("priority-high", "priority-medium", "priority-low");
                getStyleClass().add(switch (item) {
                    case "HIGH" -> "priority-high";
                    case "LOW"  -> "priority-low";
                    default     -> "priority-medium";
                });
            }
        });

        // Row colour-coding by reminder time
        remindersTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Entry item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-overdue", "row-today", "row-future", "row-completed");
                if (empty || item == null) return;

                if (item.isCompleted()) {
                    getStyleClass().add("row-completed");
                } else if (item.getReminderTime() != null) {
                    if (DateUtil.isToday(item.getReminderTime())) {
                        getStyleClass().add("row-today");
                    } else if (DateUtil.isPast(item.getReminderTime())) {
                        getStyleClass().add("row-overdue");
                    } else {
                        getStyleClass().add("row-future");
                    }
                }
            }
        });

        // Action buttons
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button doneBtn   = new Button("Done");
            private final Button editBtn   = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox   box       = new HBox(6, doneBtn, editBtn, deleteBtn);

            {
                doneBtn  .getStyleClass().add("btn-complete");
                editBtn  .getStyleClass().add("btn-edit");
                deleteBtn.getStyleClass().add("btn-delete");

                doneBtn.setOnAction(e -> {
                    Entry entry = getTableView().getItems().get(getIndex());
                    entryDAO.markAsCompleted(entry.getId());
                    loadReminders();
                    if (dashboardController != null) dashboardController.refreshDashboard();
                });

                editBtn.setOnAction(e -> {
                    Entry entry = getTableView().getItems().get(getIndex());
                    openEditDialog(entry);
                });

                deleteBtn.setOnAction(e -> {
                    Entry entry = getTableView().getItems().get(getIndex());
                    confirmAndDelete(entry);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Entry entry = getTableView().getItems().get(getIndex());
                    doneBtn.setDisable(entry.isCompleted());
                    setGraphic(box);
                }
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Data
    // ─────────────────────────────────────────────────────────────────────────

    /** Fetches all REMINDER entries from the DB and displays them. */
    public void loadReminders() {
        List<Entry> reminders = entryDAO.getEntriesByType(EntryType.REMINDER);
        remindersTable.setItems(FXCollections.observableArrayList(reminders));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Handlers
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    private void handleAddReminder() {
        Entry blank = new Entry();
        blank.setType(EntryType.REMINDER);
        AddEditEntryDialog dialog = new AddEditEntryDialog(blank);
        dialog.showAndWait().ifPresent(entry -> {
            entry.setType(EntryType.REMINDER);
            entryDAO.insertEntry(entry);
            loadReminders();
            if (dashboardController != null) dashboardController.refreshDashboard();
        });
    }

    private void openEditDialog(Entry entry) {
        AddEditEntryDialog dialog = new AddEditEntryDialog(entry);
        dialog.showAndWait().ifPresent(updated -> {
            entryDAO.updateEntry(updated);
            loadReminders();
            if (dashboardController != null) dashboardController.refreshDashboard();
        });
    }

    private void confirmAndDelete(Entry entry) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Reminder");
        confirm.setHeaderText("Delete \"" + entry.getTitle() + "\"?");
        confirm.setContentText("This action cannot be undone.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                entryDAO.deleteEntry(entry.getId());
                loadReminders();
                if (dashboardController != null) dashboardController.refreshDashboard();
            }
        });
    }
}
