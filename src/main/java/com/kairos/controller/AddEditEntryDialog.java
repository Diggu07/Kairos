package com.kairos.controller;

import com.kairos.model.Entry;
import com.kairos.model.Entry.EntryType;
import com.kairos.model.Entry.Priority;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.kairos.model.DetectedIntent;
import com.kairos.service.IntentDetectionService;

/**
 * Reusable dialog for creating or editing a Kairos {@link Entry}.
 *
 * <p>Presents a form with all editable entry fields. When {@code existingEntry}
 * is {@code null} a blank creation form is shown; otherwise the form is
 * pre-filled with the entry's current values.
 *
 * <p>On "Save", basic validation is performed (title must not be empty) before
 * the result is returned via the standard JavaFX {@link Dialog} API.
 *
 * @author Kairos
 * @version 1.0.0
 */
public class AddEditEntryDialog extends Dialog<Entry> {

    // ─────────────────────────────────────────────────────────────────────────
    // Form Controls
    // ─────────────────────────────────────────────────────────────────────────

    private final TextField   titleField        = new TextField();
    private final TextArea    contentArea       = new TextArea();
    private final ChoiceBox<EntryType> typeBox  = new ChoiceBox<>();
    private final ChoiceBox<Priority>  priorityBox = new ChoiceBox<>();
    private final TextField   tagsField         = new TextField();

    /** DatePicker for the reminder date. */
    private final DatePicker  reminderDatePicker = new DatePicker(LocalDate.now());

    /** Spinner for reminder hour (0-23). */
    private final Spinner<Integer> reminderHourSpinner   = new Spinner<>(0, 23, LocalTime.now().getHour());

    /** Spinner for reminder minute (0-59). */
    private final Spinner<Integer> reminderMinuteSpinner = new Spinner<>(0, 59, 0, 5);

    /** Row that contains the reminder date/time pickers. Only visible for REMINDER type. */
    private HBox reminderRow;

    private final Button detectIntentBtn = new Button("Detect Intent \u2728");
    private final Label intentStatusLabel = new Label();

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates the dialog.
     *
     * @param existingEntry the entry to edit, or {@code null} to create a new one
     */
    public AddEditEntryDialog(Entry existingEntry) {
        boolean isEdit = (existingEntry != null && existingEntry.getId() > 0);

        setTitle(isEdit ? "Edit Entry" : "New Entry");
        setHeaderText(isEdit ? "Edit \"" + existingEntry.getTitle() + "\"" : "Create a New Entry");

        // Style the dialog pane
        DialogPane pane = getDialogPane();
        pane.getStylesheets().add(
                getClass().getResource("/com/kairos/styles.css").toExternalForm());
        pane.getStyleClass().add("dialog-pane");
        pane.setPrefWidth(480);

        // Build form grid
        pane.setContent(buildForm(existingEntry));

        // Buttons
        ButtonType saveBtn   = new ButtonType("Save",   ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(saveBtn, cancelBtn);

        // Style the Save button
        Button saveButton = (Button) pane.lookupButton(saveBtn);
        saveButton.getStyleClass().add("btn-primary");

        // Validate before closing
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (titleField.getText().isBlank()) {
                event.consume();       // prevent dialog from closing
                titleField.setStyle("-fx-border-color: #e94560; -fx-border-width: 2;");
                titleField.setPromptText("⚠ Title is required");
                titleField.requestFocus();
            }
        });

        // Result converter — maps button press to an Entry (or null for Cancel)
        setResultConverter(buttonType -> {
            if (buttonType == saveBtn) {
                return buildEntryFromForm(existingEntry);
            }
            return null;
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Form Builder
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Constructs the form layout and pre-fills it from {@code existingEntry}.
     *
     * @param existing the entry to pre-fill from, or {@code null} for blank form
     * @return the root layout node for the dialog content area
     */
    private VBox buildForm(Entry existing) {
        // ── Title ──
        titleField.setPromptText("Entry title (required)");
        titleField.getStyleClass().add("text-field");
        if (existing != null) titleField.setText(existing.getTitle());

        // ── Content ──
        contentArea.setPromptText("Entry content… Type freely, then click 'Detect Intent'");
        contentArea.setPrefRowCount(5);
        contentArea.setWrapText(true);
        contentArea.getStyleClass().add("text-area");
        if (existing != null && existing.getContent() != null) {
            contentArea.setText(existing.getContent());
        }

        // ── Intent Detection ──
        detectIntentBtn.getStyleClass().add("btn-secondary");
        detectIntentBtn.setOnAction(e -> applyIntentDetection());
        intentStatusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px;"); // Success color
        HBox intentRow = new HBox(10, detectIntentBtn, intentStatusLabel);
        intentRow.setStyle("-fx-alignment: CENTER_LEFT;");

        // ── Type ──
        typeBox.getItems().addAll(EntryType.values());
        typeBox.setValue(existing != null ? existing.getType() : EntryType.NOTE);
        typeBox.getStyleClass().add("choice-box");
        // Show/hide reminder row based on type selection
        typeBox.setOnAction(e -> updateReminderVisibility());

        // ── Priority ──
        priorityBox.getItems().addAll(Priority.values());
        priorityBox.setValue(existing != null ? existing.getPriority() : Priority.MEDIUM);
        priorityBox.getStyleClass().add("choice-box");

        // ── Tags ──
        tagsField.setPromptText("e.g. work, urgent, personal");
        tagsField.getStyleClass().add("text-field");
        if (existing != null && existing.getTags() != null) {
            tagsField.setText(existing.getTags());
        }

        // ── Reminder date/time ──
        reminderHourSpinner  .setEditable(true);
        reminderMinuteSpinner.setEditable(true);
        reminderHourSpinner  .setPrefWidth(72);
        reminderMinuteSpinner.setPrefWidth(72);

        if (existing != null && existing.getReminderTime() != null) {
            reminderDatePicker .setValue(existing.getReminderTime().toLocalDate());
            reminderHourSpinner  .getValueFactory().setValue(existing.getReminderTime().getHour());
            reminderMinuteSpinner.getValueFactory().setValue(existing.getReminderTime().getMinute());
        }

        Label reminderLabel = new Label("Reminder Time:");
        reminderLabel.getStyleClass().add("label");

        Label colonLabel = new Label(":");
        colonLabel.getStyleClass().add("label");

        reminderRow = new HBox(8, reminderLabel, reminderDatePicker,
                reminderHourSpinner, colonLabel, reminderMinuteSpinner);
        reminderRow.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 4 0 0 0;");

        // ── Assemble grid ──
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16, 4, 4, 4));

        // Column constraints
        ColumnConstraints labelCol = new ColumnConstraints(90);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labelCol, fieldCol);

        addFormRow(grid, 0, "Title:",    titleField);
        addFormRow(grid, 1, "Content:",  new VBox(8, contentArea, intentRow));
        addFormRow(grid, 2, "Type:",     typeBox);
        addFormRow(grid, 3, "Priority:", priorityBox);
        addFormRow(grid, 4, "Tags:",     tagsField);

        VBox root = new VBox(12, grid, reminderRow);
        root.setPadding(new Insets(0, 8, 8, 8));

        updateReminderVisibility();
        return root;
    }

    /** Adds a labelled row to the given grid. */
    private void addFormRow(GridPane grid, int row, String labelText, javafx.scene.Node field) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("label");
        grid.add(lbl,   0, row);
        grid.add(field, 1, row);
    }

    /** Shows or hides the reminder date/time row based on the selected type. */
    private void updateReminderVisibility() {
        boolean isReminder = typeBox.getValue() == EntryType.REMINDER;
        reminderRow.setVisible(isReminder);
        reminderRow.setManaged(isReminder);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Entry Builder
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Reads all form fields and populates an {@link Entry}.
     * If {@code existing} is non-null its {@code id} and {@code createdAt} are preserved.
     *
     * @param existing base entry (may be {@code null})
     * @return a fully populated {@link Entry} ready to be persisted
     */
    private Entry buildEntryFromForm(Entry existing) {
        Entry entry = (existing != null) ? existing : new Entry();

        entry.setTitle   (titleField.getText().trim());
        entry.setContent (contentArea.getText());
        entry.setType    (typeBox.getValue());
        entry.setPriority(priorityBox.getValue());
        entry.setTags    (tagsField.getText().trim());
        entry.setUpdatedAt(LocalDateTime.now());

        if (entry.getCreatedAt() == null) {
            entry.setCreatedAt(LocalDateTime.now());
        }

        // Set reminder time only if REMINDER type
        if (typeBox.getValue() == EntryType.REMINDER) {
            LocalDate date = reminderDatePicker.getValue();
            if (date == null) date = LocalDate.now();
            LocalTime time = LocalTime.of(
                    reminderHourSpinner  .getValue(),
                    reminderMinuteSpinner.getValue());
            entry.setReminderTime(LocalDateTime.of(date, time));
        } else {
            entry.setReminderTime(null);
        }

        return entry;
    }

    private void applyIntentDetection() {
        String text = contentArea.getText();
        if (text == null || text.trim().isEmpty()) {
            intentStatusLabel.setText("Please enter some text first.");
            intentStatusLabel.setStyle("-fx-text-fill: #EF4444;");
            return;
        }

        DetectedIntent intent = IntentDetectionService.getInstance().detectIntent(text);
        
        // Auto-fill form fields
        typeBox.setValue(intent.getSuggestedType());
        priorityBox.setValue(intent.getSuggestedPriority());
        
        if (titleField.getText().trim().isEmpty() && intent.getExtractedTitle() != null) {
            titleField.setText(intent.getExtractedTitle());
        }
        
        if (!intent.getExtractedTags().isEmpty()) {
            String currentTags = tagsField.getText();
            String newTags = String.join(", ", intent.getExtractedTags());
            tagsField.setText(currentTags.isEmpty() ? newTags : currentTags + ", " + newTags);
        }
        
        if (intent.getSuggestedReminderTime() != null) {
            reminderDatePicker.setValue(intent.getSuggestedReminderTime().toLocalDate());
            reminderHourSpinner.getValueFactory().setValue(intent.getSuggestedReminderTime().getHour());
            reminderMinuteSpinner.getValueFactory().setValue(intent.getSuggestedReminderTime().getMinute());
        }

        intentStatusLabel.setText(String.format("Intent detected! Confidence: %s", intent.getConfidencePercentage()));
        intentStatusLabel.setStyle("-fx-text-fill: #10B981;"); // green
        
        updateReminderVisibility();
    }
}
