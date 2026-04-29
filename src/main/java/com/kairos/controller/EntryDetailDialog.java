package com.kairos.controller;

import com.kairos.dao.EntryDAO;
import com.kairos.model.Entry;
import com.kairos.model.Entry.EntryType;
import com.kairos.util.DateUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

/**
 * A read-only detail dialog that shows all fields of an Entry.
 * Provides "Edit" and "Close" buttons. If the user clicks "Edit",
 * the dialog returns the entry so the caller can open AddEditEntryDialog.
 */
public class EntryDetailDialog extends Dialog<Entry> {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy · hh:mm a");

    public EntryDetailDialog(Entry entry, DashboardController dashboardController) {
        setTitle("Entry Details");
        setHeaderText(null);

        DialogPane pane = getDialogPane();
        pane.getStylesheets().add(
                getClass().getResource("/com/kairos/styles.css").toExternalForm());
        pane.getStyleClass().add("dialog-pane");
        pane.setPrefWidth(520);

        // ── Build content ──
        VBox content = new VBox(16);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: -color-surface;");

        // ── Title + Badge Row ──
        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(entry.getTitle());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: -color-text-primary;");
        titleLabel.setWrapText(true);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label badge = new Label(entry.getType().name());
        String badgeColor = switch (entry.getType()) {
            case NOTE     -> "#3B82F6";
            case TASK     -> "#10B981";
            case REMINDER -> "#EF4444";
        };
        badge.setStyle("-fx-background-color: " + badgeColor + "; -fx-text-fill: #FFFFFF; " +
                "-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        titleRow.getChildren().addAll(titleLabel, badge);

        // ── Status Row ──
        HBox statusRow = new HBox(16);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        String priorityColor = switch (entry.getPriority()) {
            case HIGH   -> "#EF4444";
            case MEDIUM -> "#F59E0B";
            case LOW    -> "#10B981";
        };
        Label priorityLabel = new Label("● " + entry.getPriority().name());
        priorityLabel.setStyle("-fx-text-fill: " + priorityColor + "; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label statusLabel = new Label(entry.isCompleted() ? "✅ Completed" : "⏳ Pending");
        statusLabel.setStyle("-fx-text-fill: " + (entry.isCompleted() ? "#10B981" : "#F59E0B") +
                "; -fx-font-size: 13px; -fx-font-weight: bold;");

        statusRow.getChildren().addAll(priorityLabel, statusLabel);

        // ── Separator ──
        Separator sep1 = new Separator();

        // ── Content ──
        Label contentHeader = new Label("Content");
        contentHeader.setStyle("-fx-text-fill: -color-text-secondary; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label contentLabel = new Label(
                (entry.getContent() != null && !entry.getContent().isBlank())
                        ? entry.getContent()
                        : "(No content)"
        );
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.setStyle("-fx-text-fill: -color-text-primary; -fx-font-size: 14px; " +
                "-fx-padding: 12; -fx-background-color: -color-background; -fx-background-radius: 8;");

        // ── Metadata Grid ──
        Separator sep2 = new Separator();

        GridPane meta = new GridPane();
        meta.setHgap(16);
        meta.setVgap(10);

        int row = 0;

        // Tags
        if (entry.getTags() != null && !entry.getTags().isBlank()) {
            addMetaRow(meta, row++, "Tags", "🏷  " + entry.getTags());
        }

        // Reminder Time
        if (entry.getType() == EntryType.REMINDER && entry.getReminderTime() != null) {
            addMetaRow(meta, row++, "Reminder Time", "⏰  " + entry.getReminderTime().format(DATETIME_FMT));
        }

        // Created At
        if (entry.getCreatedAt() != null) {
            addMetaRow(meta, row++, "Created", "📅  " + entry.getCreatedAt().format(DATETIME_FMT));
        }

        // Updated At
        if (entry.getUpdatedAt() != null) {
            addMetaRow(meta, row++, "Last Updated", "🔄  " + entry.getUpdatedAt().format(DATETIME_FMT));
        }

        // Entry ID
        addMetaRow(meta, row++, "Entry ID", "#" + entry.getId());

        // ── Assemble ──
        content.getChildren().addAll(titleRow, statusRow, sep1, contentHeader, contentLabel, sep2, meta);

        pane.setContent(content);

        // ── Buttons ──
        ButtonType editBtn = new ButtonType("✏  Edit", ButtonBar.ButtonData.LEFT);
        ButtonType markDoneBtn = entry.isCompleted()
                ? new ButtonType("↩  Mark Pending", ButtonBar.ButtonData.OTHER)
                : new ButtonType("✅  Mark Complete", ButtonBar.ButtonData.OTHER);
        ButtonType deleteBtn = new ButtonType("🗑  Delete", ButtonBar.ButtonData.OTHER);
        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);

        pane.getButtonTypes().addAll(editBtn, markDoneBtn, deleteBtn, closeBtn);

        // Style the buttons
        Button editButton = (Button) pane.lookupButton(editBtn);
        editButton.getStyleClass().add("btn-primary");

        Button markButton = (Button) pane.lookupButton(markDoneBtn);
        markButton.getStyleClass().add("btn-complete");

        Button deleteButton = (Button) pane.lookupButton(deleteBtn);
        deleteButton.setStyle("-fx-background-color: #EF4444; -fx-text-fill: #FFFFFF; -fx-background-radius: 6;");

        // ── Result converter ──
        setResultConverter(buttonType -> {
            EntryDAO dao = new EntryDAO();

            if (buttonType == editBtn) {
                // Open the edit dialog
                AddEditEntryDialog editDialog = new AddEditEntryDialog(entry);
                editDialog.showAndWait().ifPresent(updatedEntry -> {
                    dao.updateEntry(updatedEntry);
                    if (dashboardController != null) {
                        dashboardController.refreshDashboard();
                    }
                });
            } else if (buttonType == markDoneBtn) {
                if (entry.isCompleted()) {
                    // Mark as pending
                    entry.setCompleted(false);
                    dao.updateEntry(entry);
                } else {
                    dao.markAsCompleted(entry.getId());
                }
                if (dashboardController != null) {
                    dashboardController.refreshDashboard();
                }
            } else if (buttonType == deleteBtn) {
                // Confirm deletion
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Entry");
                confirm.setHeaderText("Are you sure?");
                confirm.setContentText("This will permanently delete \"" + entry.getTitle() + "\".");
                confirm.getDialogPane().getStylesheets().add(
                        getClass().getResource("/com/kairos/styles.css").toExternalForm());
                confirm.getDialogPane().getStyleClass().add("dialog-pane");

                confirm.showAndWait().ifPresent(result -> {
                    if (result == ButtonType.OK) {
                        dao.deleteEntry(entry.getId());
                        if (dashboardController != null) {
                            dashboardController.refreshDashboard();
                        }
                    }
                });
            }
            return null;
        });
    }

    private void addMetaRow(GridPane grid, int row, String label, String value) {
        Label keyLabel = new Label(label);
        keyLabel.setStyle("-fx-text-fill: -color-text-secondary; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label valLabel = new Label(value);
        valLabel.setStyle("-fx-text-fill: -color-text-primary; -fx-font-size: 13px;");
        valLabel.setWrapText(true);

        grid.add(keyLabel, 0, row);
        grid.add(valLabel, 1, row);
    }
}
