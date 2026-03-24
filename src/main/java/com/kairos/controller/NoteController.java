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
 * Controller for the Notes TableView ({@code note-view.fxml}).
 *
 * <p>Displays all {@link EntryType#NOTE} entries in a sortable table.
 * Supports add, edit, and delete via the {@link AddEditEntryDialog}.
 *
 * @author Kairos
 * @version 1.0.0
 */
public class NoteController implements Initializable {

    // ─────────────────────────────────────────────────────────────────────────
    // FXML Injections
    // ─────────────────────────────────────────────────────────────────────────

    @FXML private TableView<Entry>              notesTable;
    @FXML private TableColumn<Entry, String>    colTitle;
    @FXML private TableColumn<Entry, String>    colTags;
    @FXML private TableColumn<Entry, String>    colPriority;
    @FXML private TableColumn<Entry, String>    colCreated;
    @FXML private TableColumn<Entry, Void>      colActions;

    // ─────────────────────────────────────────────────────────────────────────
    // State
    // ─────────────────────────────────────────────────────────────────────────

    private final EntryDAO         entryDAO = new EntryDAO();
    private       DashboardController dashboardController;

    // ─────────────────────────────────────────────────────────────────────────
    // Initialise
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sets up table columns and loads all NOTE entries.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        loadNotes();
    }

    /**
     * Injects a reference to the parent dashboard controller so that CRUD
     * operations can trigger a full dashboard refresh.
     *
     * @param controller the active {@link DashboardController}
     */
    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Table Setup
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Configures cell-value factories and the action button column.
     */
    private void setupColumns() {
        colTitle   .setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTitle()));
        colTags    .setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getTags() != null ? cd.getValue().getTags() : ""));
        colPriority.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getPriority().name()));
        colCreated .setCellValueFactory(cd -> new SimpleStringProperty(
                DateUtil.formatDisplay(cd.getValue().getCreatedAt())));

        // Style priority cells
        colPriority.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                getStyleClass().removeAll("priority-high", "priority-medium", "priority-low");
                getStyleClass().add(switch (item) {
                    case "HIGH"   -> "priority-high";
                    case "LOW"    -> "priority-low";
                    default       -> "priority-medium";
                });
            }
        });

        // Action buttons column
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn   = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox   box       = new HBox(6, editBtn, deleteBtn);

            {
                editBtn  .getStyleClass().add("btn-edit");
                deleteBtn.getStyleClass().add("btn-delete");

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
                setGraphic(empty ? null : box);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Data Loading
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fetches all NOTE entries from the database and populates the table.
     */
    public void loadNotes() {
        List<Entry> notes = entryDAO.getEntriesByType(EntryType.NOTE);
        notesTable.setItems(FXCollections.observableArrayList(notes));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FXML Handlers
    // ─────────────────────────────────────────────────────────────────────────

    /** Opens the creation dialog for a new NOTE entry. */
    @FXML
    private void handleAddNote() {
        Entry blank = new Entry();
        blank.setType(EntryType.NOTE);
        AddEditEntryDialog dialog = new AddEditEntryDialog(blank);
        dialog.showAndWait().ifPresent(entry -> {
            entry.setType(EntryType.NOTE);
            entryDAO.insertEntry(entry);
            loadNotes();
            if (dashboardController != null) dashboardController.refreshDashboard();
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Opens the edit dialog pre-filled with the given entry.
     *
     * @param entry the entry to edit
     */
    private void openEditDialog(Entry entry) {
        AddEditEntryDialog dialog = new AddEditEntryDialog(entry);
        dialog.showAndWait().ifPresent(updated -> {
            entryDAO.updateEntry(updated);
            loadNotes();
            if (dashboardController != null) dashboardController.refreshDashboard();
        });
    }

    /**
     * Shows a confirmation alert and, if confirmed, deletes the entry.
     *
     * @param entry the entry to delete
     */
    private void confirmAndDelete(Entry entry) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Note");
        confirm.setHeaderText("Delete \"" + entry.getTitle() + "\"?");
        confirm.setContentText("This action cannot be undone.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                entryDAO.deleteEntry(entry.getId());
                loadNotes();
                if (dashboardController != null) dashboardController.refreshDashboard();
            }
        });
    }
}
