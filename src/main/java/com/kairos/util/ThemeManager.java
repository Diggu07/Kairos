package com.kairos.util;

import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    private static ThemeManager instance;
    private List<ThemeProfile> availableThemes;
    private ThemeProfile currentTheme;

    private ThemeManager() {
        availableThemes = new ArrayList<>();
        initializeThemes();
        if (!availableThemes.isEmpty()) {
            currentTheme = availableThemes.get(0);
        }
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    private void initializeThemes() {
        // Default Dark Theme
        ThemeProfile darkTheme = new ThemeProfile("Kairos Dark", "Professional dark theme");
        darkTheme.setPrimary(Color.web("#3B82F6"));
        darkTheme.setSecondary(Color.web("#10B981"));
        darkTheme.setBackground(Color.web("#0F172A"));
        darkTheme.setSurface(Color.web("#1E293B"));
        darkTheme.setError(Color.web("#EF4444"));
        darkTheme.setSuccess(Color.web("#10B981"));
        darkTheme.setWarning(Color.web("#F59E0B"));
        darkTheme.setTextPrimary(Color.web("#F1F5F9"));
        darkTheme.setTextSecondary(Color.web("#94A3B8"));
        availableThemes.add(darkTheme);

        // Light Theme
        ThemeProfile lightTheme = new ThemeProfile("Kairos Light", "Clean light theme");
        lightTheme.setPrimary(Color.web("#2563EB"));
        lightTheme.setSecondary(Color.web("#059669"));
        lightTheme.setBackground(Color.web("#F8FAFC"));
        lightTheme.setSurface(Color.web("#FFFFFF"));
        lightTheme.setError(Color.web("#DC2626"));
        lightTheme.setSuccess(Color.web("#059669"));
        lightTheme.setWarning(Color.web("#D97706"));
        lightTheme.setTextPrimary(Color.web("#0F172A"));
        lightTheme.setTextSecondary(Color.web("#475569"));
        availableThemes.add(lightTheme);
    }

    public void applyTheme(ThemeProfile theme) {
        if (availableThemes.contains(theme)) {
            this.currentTheme = theme;
            // Here you would typically apply the CSS globally
            // e.g., using a global stylesheet updated dynamically
        }
    }

    public ThemeProfile getCurrentTheme() {
        return currentTheme;
    }

    public List<ThemeProfile> getAvailableThemes() {
        return availableThemes;
    }

    public ThemeProfile createCustomTheme() {
        ThemeProfile custom = new ThemeProfile("Custom Theme", "User defined theme");
        availableThemes.add(custom);
        return custom;
    }
}
