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
 * Controller for the Tasks TableView ({@code task-view.fxml}).
 *
 * <p>Displays all {@link EntryType#TASK} entries with a completion
 * progress bar. Supports add, edit, delete and mark-as-complete actions.
 *
 * @author Kairos
 * @version 1.0.0
 */
public class TaskController implements Initializable {

    // ─────────────────────────────────────────────────────────────────────────
    // FXML Injections
    // ─────────────────────────────────────────────────────────────────────────

    @FXML private TableView<Entry>           tasksTable;
    @FXML private TableColumn<Entry, String> colTitle;
    @FXML private TableColumn<Entry, String> colPriority;
    @FXML private TableColumn<Entry, String> colStatus;
    @FXML private TableColumn<Entry, String> colUpdated;
    @FXML private TableColumn<Entry, Void>   colActions;
    @FXML private ProgressBar                progressBar;
    @FXML private Label                      progressLabel;

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
        loadTasks();
    }

    /** Injects the parent dashboard reference for post-CRUD refreshes. */
    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Table Setup
    // ─────────────────────────────────────────────────────────────────────────

    private void setupColumns() {
        colTitle   .setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTitle()));
        colPriority.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getPriority().name()));
        colStatus  .setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().isCompleted() ? "✔ Completed" : "⏳ Pending"));
        colUpdated .setCellValueFactory(cd -> new SimpleStringProperty(
                DateUtil.formatDisplay(cd.getValue().getUpdatedAt())));

        // Priority colour coding
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

        // Status colour coding
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (item.startsWith("✔")) {
                    setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #ff9800;");
                }
            }
        });

        // Row style for completed tasks
        tasksTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Entry item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("row-completed");
                if (!empty && item != null && item.isCompleted()) {
                    getStyleClass().add("row-completed");
                }
            }
        });

        // Action buttons
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button completeBtn = new Button("Complete");
            private final Button editBtn     = new Button("Edit");
            private final Button deleteBtn   = new Button("Delete");
            private final HBox   box         = new HBox(5, completeBtn, editBtn, deleteBtn);

            {
                completeBtn.getStyleClass().add("btn-complete");
                editBtn    .getStyleClass().add("btn-edit");
                deleteBtn  .getStyleClass().add("btn-delete");

                completeBtn.setOnAction(e -> {
                    Entry entry = getTableView().getItems().get(getIndex());
                    entryDAO.markAsCompleted(entry.getId());
                    loadTasks();
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
                if (empty) { setGraphic(null); return; }
                Entry e = getTableView().getItems().get(getIndex());
                completeBtn.setDisable(e.isCompleted());
                setGraphic(box);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Data
    // ─────────────────────────────────────────────────────────────────────────

    /** Fetches all TASK entries and updates the table and progress bar. */
    public void loadTasks() {
        List<Entry> tasks = entryDAO.getEntriesByType(EntryType.TASK);
        tasksTable.setItems(FXCollections.observableArrayList(tasks));
        updateProgress(tasks);
    }

    /**
     * Recalculates and displays the completion percentage.
     *
     * @param tasks the full list of task entries
     */
    private void updateProgress(List<Entry> tasks) {
        if (tasks.isEmpty()) {
            progressBar.setProgress(0);
            progressLabel.setText("0 / 0 completed");
            return;
        }
        long done  = tasks.stream().filter(Entry::isCompleted).count();
        double pct = (double) done / tasks.size();
        progressBar.setProgress(pct);
        progressLabel.setText(done + " / " + tasks.size() + " completed");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Handlers
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    private void handleAddTask() {
        Entry blank = new Entry();
        blank.setType(EntryType.TASK);
        AddEditEntryDialog dialog = new AddEditEntryDialog(blank);
        dialog.showAndWait().ifPresent(entry -> {
            entry.setType(EntryType.TASK);
            entryDAO.insertEntry(entry);
            loadTasks();
            if (dashboardController != null) dashboardController.refreshDashboard();
        });
    }

    private void openEditDialog(Entry entry) {
        AddEditEntryDialog dialog = new AddEditEntryDialog(entry);
        dialog.showAndWait().ifPresent(updated -> {
            entryDAO.updateEntry(updated);
            loadTasks();
            if (dashboardController != null) dashboardController.refreshDashboard();
        });
    }

    private void confirmAndDelete(Entry entry) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Task");
        confirm.setHeaderText("Delete \"" + entry.getTitle() + "\"?");
        confirm.setContentText("This action cannot be undone.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                entryDAO.deleteEntry(entry.getId());
                loadTasks();
                if (dashboardController != null) dashboardController.refreshDashboard();
            }
        });
    }
}
