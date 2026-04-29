package com.kairos.controller;

import com.kairos.dao.EntryDAO;
import com.kairos.model.Entry;
import com.kairos.model.Entry.EntryType;
import com.kairos.util.DateUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Controller for the main dashboard layout ({@code dashboard.fxml}).
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Loads summary counts from {@link EntryDAO} on startup.</li>
 *   <li>Renders the ten most-recently updated entries as cards.</li>
 *   <li>Handles sidebar navigation (swaps the centre content pane).</li>
 *   <li>Live-filters entries via the search field and type combo box.</li>
 *   <li>Opens the {@link AddEditEntryDialog} for new entry creation.</li>
 * </ul>
 *
 * @author Kairos
 * @version 1.0.0
 */
public class DashboardController implements Initializable {

    // ─────────────────────────────────────────────────────────────────────────
    // FXML Injections
    // ─────────────────────────────────────────────────────────────────────────

    /* Sidebar nav buttons */
    @FXML private Button btnDashboard;
    @FXML private Button btnNotes;
    @FXML private Button btnTasks;
    @FXML private Button btnReminders;
    @FXML private Button btnSettings;

    /* Top bar */
    @FXML private TextField  searchField;
    @FXML private ComboBox<String> filterCombo;

    /* Home content */
    @FXML private ScrollPane mainScrollPane;
    @FXML private VBox       homeContent;
    @FXML private Label      countNotes;
    @FXML private Label      countTasks;
    @FXML private Label      countReminders;
    @FXML private Label      countCompleted;
    @FXML private VBox       recentEntriesBox;
    @FXML private Label      emptyStateLabel;
    @FXML private TextField  smartEntryField;

    // ─────────────────────────────────────────────────────────────────────────
    // State
    // ─────────────────────────────────────────────────────────────────────────

    private final EntryDAO entryDAO = new EntryDAO();

    /** Tracks which nav button is currently "active" for CSS styling. */
    private Button activeNavBtn;

    // ─────────────────────────────────────────────────────────────────────────
    // Initialise
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called by JavaFX after all @FXML fields are injected.
     * Seeds the filter combo, attaches the live-search listener,
     * and loads initial dashboard data.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activeNavBtn = btnDashboard;

        // Populate type filter combo
        filterCombo.setItems(FXCollections.observableArrayList(
                "All", "Notes", "Tasks", "Reminders"));
        filterCombo.setValue("All");
        filterCombo.setOnAction(e -> applyFilter());

        // Live search on every keystroke
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        refreshDashboard();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Dashboard Data
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Reloads all summary counts and the recent-entries list.
     * Call this after any CRUD operation to keep the UI in sync.
     */
    public void refreshDashboard() {
        loadSummaryCounts();
        applyFilter();
    }

    /**
     * Queries the database for aggregate counts and updates the four
     * summary cards.
     */
    private void loadSummaryCounts() {
        long notes     = entryDAO.getEntriesByType(EntryType.NOTE).size();
        long pendingTasks = entryDAO.getEntriesByType(EntryType.TASK)
                .stream().filter(e -> !e.isCompleted()).count();
        long upcoming  = entryDAO.getEntriesByType(EntryType.REMINDER)
                .stream().filter(e -> !e.isCompleted() && DateUtil.isFuture(e.getReminderTime())).count();
        long doneToday = entryDAO.getAllEntries()
                .stream().filter(e -> e.isCompleted()
                        && e.getUpdatedAt() != null
                        && e.getUpdatedAt().toLocalDate().isEqual(LocalDate.now())).count();

        countNotes    .setText(String.valueOf(notes));
        countTasks    .setText(String.valueOf(pendingTasks));
        countReminders.setText(String.valueOf(upcoming));
        countCompleted.setText(String.valueOf(doneToday));
    }

    /**
     * Applies the current search keyword and type filter, then re-renders
     * the entry cards.
     */
    private void applyFilter() {
        String keyword    = searchField.getText().trim();
        String filterType = filterCombo.getValue();

        List<Entry> results = entryDAO.searchEntries(keyword);

        // Apply type filter on top of keyword filter
        if (filterType != null && !filterType.equals("All")) {
            EntryType desired = switch (filterType) {
                case "Notes"     -> EntryType.NOTE;
                case "Tasks"     -> EntryType.TASK;
                case "Reminders" -> EntryType.REMINDER;
                default          -> null;
            };
            if (desired != null) {
                final EntryType ft = desired;
                results = results.stream().filter(e -> e.getType() == ft).toList();
            }
        }

        renderEntryCards(results);
    }

    /**
     * Clears and re-populates the recent entries area with cards for the
     * given list (shows at most 10).
     *
     * @param entries the list of entries to display
     */
    private void renderEntryCards(List<Entry> entries) {
        recentEntriesBox.getChildren().clear();

        boolean empty = entries.isEmpty();
        emptyStateLabel.setVisible(empty);
        emptyStateLabel.setManaged(empty);

        entries.stream().limit(10).forEach(entry -> {
            VBox card = buildEntryCard(entry);
            recentEntriesBox.getChildren().add(card);
        });
    }

    /**
     * Builds a visual card node for the given entry.
     *
     * @param entry the entry to represent
     * @return a styled {@link VBox} card node
     */
    private VBox buildEntryCard(Entry entry) {
        VBox card = new VBox(6);
        card.getStyleClass().add("entry-card");
        card.setPadding(new Insets(15));

        // ── Header row: title + type badge ──
        HBox header = new HBox(8);
        header.setStyle("-fx-alignment: CENTER_LEFT;");

        Label title = new Label(entry.getTitle());
        title.getStyleClass().add("entry-title");
        title.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(title, Priority.ALWAYS);

        Label badge = new Label(entry.getType().name());
        badge.getStyleClass().addAll("entry-type-badge",
                switch (entry.getType()) {
                    case NOTE     -> "badge-note";
                    case TASK     -> "badge-task";
                    case REMINDER -> "badge-reminder";
                });

        header.getChildren().addAll(title, badge);

        // ── Priority ──
        Label priorityLabel = new Label("Priority: " + entry.getPriority().name());
        priorityLabel.getStyleClass().addAll("entry-meta",
                switch (entry.getPriority()) {
                    case HIGH   -> "priority-high";
                    case MEDIUM -> "priority-medium";
                    case LOW    -> "priority-low";
                });

        // ── Metadata row ──
        HBox meta = new HBox(16);
        Label dateLabel = new Label("Updated: " + DateUtil.formatDisplay(entry.getUpdatedAt()));
        dateLabel.getStyleClass().add("entry-meta");

        Label tagsLabel = new Label(
                (entry.getTags() != null && !entry.getTags().isBlank())
                        ? "🏷 " + entry.getTags() : "");
        tagsLabel.getStyleClass().add("entry-meta");

        meta.getChildren().addAll(dateLabel, tagsLabel);

        // ── Content snippet ──
        if (entry.getContent() != null && !entry.getContent().isBlank()) {
            String snippet = entry.getContent().length() > 120
                    ? entry.getContent().substring(0, 120) + "…"
                    : entry.getContent();
            Label contentLabel = new Label(snippet);
            contentLabel.getStyleClass().add("entry-meta");
            contentLabel.setWrapText(true);
            card.getChildren().addAll(header, priorityLabel, contentLabel, meta);
        } else {
            card.getChildren().addAll(header, priorityLabel, meta);
        }

        // ── Click to open detail view ──
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(e -> {
            EntryDetailDialog detailDialog = new EntryDetailDialog(entry, this);
            detailDialog.showAndWait();
        });

        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sidebar Navigation
    // ─────────────────────────────────────────────────────────────────────────

    /** Navigates back to the default dashboard home view. */
    @FXML
    private void handleNavDashboard() {
        setActiveNav(btnDashboard);
        mainScrollPane.setContent(homeContent);
        refreshDashboard();
    }

    /** Loads the Notes TableView into the centre pane. */
    @FXML
    private void handleNavNotes() {
        setActiveNav(btnNotes);
        loadView("/com/kairos/note-view.fxml");
    }

    /** Loads the Tasks TableView. */
    @FXML
    private void handleNavTasks() {
        setActiveNav(btnTasks);
        loadView("/com/kairos/task-view.fxml");
    }

    /** Loads the Reminders TableView. */
    @FXML
    private void handleNavReminders() {
        setActiveNav(btnReminders);
        loadView("/com/kairos/reminder-view.fxml");
    }

    /** Opens Settings & Insights panel with analytics, procrastination, and notification tests. */
    @FXML
    private void handleNavSettings() {
        setActiveNav(btnSettings);

        VBox settingsPanel = new VBox(20);
        settingsPanel.setStyle("-fx-padding: 24;");

        // ── Title ──
        Label title = new Label("⚙  Settings & Insights");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: -color-text-primary;");

        // ══════════════════════════════════════════════════
        // SECTION 1: Analytics Overview
        // ══════════════════════════════════════════════════
        com.kairos.service.AnalyticsService analytics = com.kairos.service.AnalyticsService.getInstance();

        Label analyticsTitle = new Label("📊  Analytics Overview");
        analyticsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -color-text-primary;");

        double completionRate = analytics.getTaskCompletionRate(
                java.time.LocalDate.now().minusDays(30), java.time.LocalDate.now());
        int streak = analytics.getCurrentStreak();
        int habitScore = analytics.getHabitScore();
        java.time.LocalTime productiveHour = analytics.getMostProductiveHour();
        java.time.DayOfWeek productiveDay = analytics.getMostProductiveDay();

        HBox analyticsCards = new HBox(12);
        analyticsCards.getChildren().addAll(
            buildInfoCard("Completion Rate", String.format("%.0f%%", completionRate * 100), "#10B981"),
            buildInfoCard("Current Streak", streak + " days", "#F59E0B"),
            buildInfoCard("Habit Score", habitScore + "/100", "#3B82F6"),
            buildInfoCard("Best Hour", productiveHour.toString(), "#8B5CF6"),
            buildInfoCard("Best Day", productiveDay.toString(), "#EC4899")
        );

        // ══════════════════════════════════════════════════
        // SECTION 2: Procrastination Analysis
        // ══════════════════════════════════════════════════
        com.kairos.service.ProcrastinationDetector detector = new com.kairos.service.ProcrastinationDetector();

        Label procTitle = new Label("🔍  Procrastination Analysis");
        procTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -color-text-primary;");

        var stalledTasks = detector.identifyProcrastinatedTasks();
        var level = detector.getProcrastinationLevel();

        String levelColor = switch (level) {
            case HIGH -> "#EF4444";
            case MODERATE -> "#F59E0B";
            case LOW -> "#10B981";
        };

        Label levelLabel = new Label("Level: " + level.name());
        levelLabel.setStyle("-fx-text-fill: " + levelColor + "; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label stalledLabel = new Label("Stalled items: " + stalledTasks.size());
        stalledLabel.setStyle("-fx-text-fill: -color-text-secondary; -fx-font-size: 13px;");

        Label actionLabel = new Label("💡 " + detector.suggestImmediateAction());
        actionLabel.setWrapText(true);
        actionLabel.setStyle("-fx-text-fill: -color-text-primary; -fx-font-size: 13px; -fx-padding: 8; " +
                "-fx-background-color: -color-surface; -fx-background-radius: 8; -fx-border-color: -color-border-custom; -fx-border-radius: 8;");

        VBox procBox = new VBox(8, levelLabel, stalledLabel, actionLabel);
        procBox.setStyle("-fx-padding: 12; -fx-background-color: -color-surface; -fx-background-radius: 10; " +
                "-fx-border-color: -color-border-custom; -fx-border-radius: 10;");

        // ══════════════════════════════════════════════════
        // SECTION 3: Motivation
        // ══════════════════════════════════════════════════
        com.kairos.service.MotivationService motivation = com.kairos.service.MotivationService.getInstance();
        var prompt = motivation.generateMotivationalPrompt();

        Label motTitle = new Label("🌟  Motivation");
        motTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -color-text-primary;");

        Label quoteLabel = new Label("\"" + prompt.title + "\"");
        quoteLabel.setStyle("-fx-font-size: 15px; -fx-font-style: italic; -fx-text-fill: -color-primary;");

        Label quoteMsg = new Label(prompt.message);
        quoteMsg.setWrapText(true);
        quoteMsg.setStyle("-fx-text-fill: -color-text-secondary; -fx-font-size: 13px;");

        Label motScoreLabel = new Label("Motivation Score: " + motivation.getMotivationScore() + "/100");
        motScoreLabel.setStyle("-fx-text-fill: -color-text-primary; -fx-font-weight: bold;");

        VBox motBox = new VBox(8, quoteLabel, quoteMsg, motScoreLabel);
        motBox.setStyle("-fx-padding: 12; -fx-background-color: -color-surface; -fx-background-radius: 10; " +
                "-fx-border-color: -color-border-custom; -fx-border-radius: 10;");

        // ══════════════════════════════════════════════════
        // SECTION 4: Test Buttons
        // ══════════════════════════════════════════════════
        Label testTitle = new Label("🧪  Test Features");
        testTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -color-text-primary;");

        Button testToast = new Button("🔔 Test Toast Notification");
        testToast.getStyleClass().add("btn-primary");
        testToast.setOnAction(e -> {
            com.kairos.service.AlertManager.getInstance().triggerAlert(
                com.kairos.service.AlertManager.AlertType.MILESTONE_REACHED, null);
        });

        Button testWarning = new Button("⚠ Test Warning Alert");
        testWarning.getStyleClass().add("btn-edit");
        testWarning.setOnAction(e -> {
            com.kairos.service.AlertManager.getInstance().triggerAlert(
                com.kairos.service.AlertManager.AlertType.TASK_OVERDUE, null);
        });

        Button testMotivation = new Button("💪 Test Motivation Prompt");
        testMotivation.getStyleClass().add("btn-complete");
        testMotivation.setOnAction(e -> {
            com.kairos.service.AlertManager.getInstance().triggerAlert(
                com.kairos.service.AlertManager.AlertType.MOTIVATION_PROMPT, null);
        });

        Button testBackup = new Button("💾 Create Backup");
        testBackup.getStyleClass().add("btn-secondary");
        testBackup.setOnAction(e -> {
            com.kairos.service.BackupService backupService = new com.kairos.service.BackupService();
            String path = backupService.createBackup(
                System.getProperty("user.home") + "/kairos/kairos.db");
            if (path != null) {
                com.kairos.components.CustomNotificationWindow.showNotification(
                    "Backup Created", "Saved to: " + path,
                    com.kairos.components.CustomNotificationWindow.NotificationType.SUCCESS);
            } else {
                com.kairos.components.CustomNotificationWindow.showNotification(
                    "Backup Failed", "Could not create backup.",
                    com.kairos.components.CustomNotificationWindow.NotificationType.ERROR);
            }
        });

        HBox testButtons = new HBox(10, testToast, testWarning, testMotivation, testBackup);

        // ── Assemble ──
        settingsPanel.getChildren().addAll(
            title,
            new Separator(),
            analyticsTitle, analyticsCards,
            new Separator(),
            procTitle, procBox,
            new Separator(),
            motTitle, motBox,
            new Separator(),
            testTitle, testButtons
        );

        mainScrollPane.setContent(settingsPanel);
    }

    /** Helper to build a small stat card for the settings panel. */
    private VBox buildInfoCard(String label, String value, String color) {
        VBox card = new VBox(4);
        card.setStyle("-fx-padding: 14; -fx-background-color: -color-surface; -fx-background-radius: 10; " +
                "-fx-border-color: -color-border-custom; -fx-border-radius: 10; -fx-min-width: 120;");
        card.getStyleClass().add("summary-card");

        Label valLabel = new Label(value);
        valLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: -color-text-secondary;");

        card.getChildren().addAll(valLabel, nameLabel);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    /**
     * Updates CSS "active" class on the sidebar navigation buttons.
     *
     * @param newActive the button that should appear highlighted
     */
    private void setActiveNav(Button newActive) {
        if (activeNavBtn != null) {
            activeNavBtn.getStyleClass().remove("active");
        }
        newActive.getStyleClass().add("active");
        activeNavBtn = newActive;
    }

    /**
     * Loads an FXML resource and sets it as the centre scroll-pane content.
     *
     * @param fxmlPath classpath-relative path to the FXML file
     */
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Node view = loader.load();

            // Pass a reference to this controller so sub-views can trigger refresh
            Object ctrl = loader.getController();
            if (ctrl instanceof NoteController nc)     nc.setDashboardController(this);
            if (ctrl instanceof TaskController tc)     tc.setDashboardController(this);
            if (ctrl instanceof ReminderController rc) rc.setDashboardController(this);

            mainScrollPane.setContent(view);
        } catch (IOException e) {
            System.err.println("[DashboardController] Failed to load view " + fxmlPath + ": " + e.getMessage());
            showErrorAlert("Navigation Error", "Could not load the requested view.", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // New Entry
    // ─────────────────────────────────────────────────────────────────────────

    /** Opens the Add/Edit dialog for creating a brand-new entry. */
    @FXML
    private void handleNewEntry() {
        AddEditEntryDialog dialog = new AddEditEntryDialog(null);
        dialog.showAndWait().ifPresent(entry -> {
            int newId = entryDAO.insertEntry(entry);
            if (newId > 0) {
                refreshDashboard();
            } else {
                showErrorAlert("Save Failed", "Could not save the entry.", "Check the logs for details.");
            }
        });
    }

    /** Detects intent from the smart text field and opens pre-filled dialog. */
    @FXML
    private void handleSmartEntry() {
        String text = smartEntryField.getText();
        if (text == null || text.isBlank()) {
            showErrorAlert("Empty Input", "Please enter some text.", "You must type something before detecting intent.");
            return;
        }

        com.kairos.model.DetectedIntent intent = com.kairos.service.IntentDetectionService.getInstance().detectIntent(text);

        Entry prefilled = new Entry();
        prefilled.setTitle(intent.getExtractedTitle() != null ? intent.getExtractedTitle() : "New Entry");
        prefilled.setContent(intent.getExtractedContent() != null ? intent.getExtractedContent() : text);
        prefilled.setType(intent.getSuggestedType() != null ? intent.getSuggestedType() : EntryType.NOTE);
        prefilled.setPriority(intent.getSuggestedPriority() != null ? intent.getSuggestedPriority() : com.kairos.model.Entry.Priority.MEDIUM);
        
        if (intent.getSuggestedReminderTime() != null) {
            prefilled.setReminderTime(intent.getSuggestedReminderTime());
        }
        
        if (intent.getExtractedTags() != null && !intent.getExtractedTags().isEmpty()) {
            prefilled.setTags(String.join(", ", intent.getExtractedTags()));
        }

        AddEditEntryDialog dialog = new AddEditEntryDialog(prefilled);
        dialog.showAndWait().ifPresent(entry -> {
            int newId = entryDAO.insertEntry(entry);
            if (newId > 0) {
                smartEntryField.clear();
                refreshDashboard();
            } else {
                showErrorAlert("Save Failed", "Could not save the entry.", "Check the logs for details.");
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Shows a styled error alert dialog.
     *
     * @param title   window title
     * @param header  bold header text
     * @param content detail message
     */
    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
