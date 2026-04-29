package com.kairos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import com.kairos.service.AnalyticsService;

public class AnalyticsDashboardController {

    @FXML private Label completionRateLabel;
    @FXML private Label currentStreakLabel;
    @FXML private Label habitScoreLabel;
    
    @FXML private LineChart<String, Number> trendChart;
    @FXML private PieChart distributionChart;
    
    @FXML private VBox insightsBox;

    private AnalyticsService analyticsService;

    public void initialize() {
        analyticsService = AnalyticsService.getInstance();
        loadMetrics();
        setupCharts();
        generateInsights();
    }

    private void loadMetrics() {
        completionRateLabel.setText(String.format("%.0f%%", analyticsService.getTaskCompletionRate(null, null) * 100));
        currentStreakLabel.setText(analyticsService.getCurrentStreak() + " Days");
        habitScoreLabel.setText(String.valueOf(analyticsService.getHabitScore()));
    }

    private void setupCharts() {
        // Setup Line Chart stub
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tasks Completed");
        series.getData().add(new XYChart.Data<>("Mon", 3));
        series.getData().add(new XYChart.Data<>("Tue", 5));
        series.getData().add(new XYChart.Data<>("Wed", 4));
        trendChart.getData().add(series);

        // Setup Pie Chart stub
        distributionChart.getData().addAll(
            new PieChart.Data("Notes", 40),
            new PieChart.Data("Tasks", 45),
            new PieChart.Data("Reminders", 15)
        );
    }

    private void generateInsights() {
        insightsBox.getChildren().clear();
        Label insight1 = new Label("💡 You are most productive on " + analyticsService.getMostProductiveDay() + "s at " + analyticsService.getMostProductiveHour());
        insight1.setStyle("-fx-text-fill: var(--text-primary); -fx-padding: 5 0;");
        
        Label insight2 = new Label("🔥 You are on a " + analyticsService.getCurrentStreak() + "-day streak! Keep it up.");
        insight2.setStyle("-fx-text-fill: var(--text-primary); -fx-padding: 5 0;");
        
        insightsBox.getChildren().addAll(insight1, insight2);
    }
}
