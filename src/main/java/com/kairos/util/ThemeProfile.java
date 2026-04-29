package com.kairos.util;

import javafx.scene.paint.Color;

public class ThemeProfile {
    private String name;
    private String description;
    
    private Color primary;
    private Color secondary;
    private Color background;
    private Color surface;
    private Color error;
    private Color success;
    private Color warning;
    private Color textPrimary;
    private Color textSecondary;
    
    private String fontFamily;
    private double fontSize;
    
    public ThemeProfile(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Color getPrimary() { return primary; }
    public void setPrimary(Color primary) { this.primary = primary; }

    public Color getSecondary() { return secondary; }
    public void setSecondary(Color secondary) { this.secondary = secondary; }

    public Color getBackground() { return background; }
    public void setBackground(Color background) { this.background = background; }

    public Color getSurface() { return surface; }
    public void setSurface(Color surface) { this.surface = surface; }

    public Color getError() { return error; }
    public void setError(Color error) { this.error = error; }

    public Color getSuccess() { return success; }
    public void setSuccess(Color success) { this.success = success; }

    public Color getWarning() { return warning; }
    public void setWarning(Color warning) { this.warning = warning; }

    public Color getTextPrimary() { return textPrimary; }
    public void setTextPrimary(Color textPrimary) { this.textPrimary = textPrimary; }

    public Color getTextSecondary() { return textSecondary; }
    public void setTextSecondary(Color textSecondary) { this.textSecondary = textSecondary; }

    public String getFontFamily() { return fontFamily; }
    public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }

    public double getFontSize() { return fontSize; }
    public void setFontSize(double fontSize) { this.fontSize = fontSize; }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    public String generateCSS() {
        StringBuilder css = new StringBuilder();
        css.append(":root {\n");
        if (primary != null) css.append("    --primary-color: ").append(toHexString(primary)).append(";\n");
        if (secondary != null) css.append("    --secondary-color: ").append(toHexString(secondary)).append(";\n");
        if (background != null) css.append("    --background-color: ").append(toHexString(background)).append(";\n");
        if (surface != null) css.append("    --surface-color: ").append(toHexString(surface)).append(";\n");
        if (error != null) css.append("    --error-color: ").append(toHexString(error)).append(";\n");
        if (success != null) css.append("    --success-color: ").append(toHexString(success)).append(";\n");
        if (warning != null) css.append("    --warning-color: ").append(toHexString(warning)).append(";\n");
        if (textPrimary != null) css.append("    --text-primary: ").append(toHexString(textPrimary)).append(";\n");
        if (textSecondary != null) css.append("    --text-secondary: ").append(toHexString(textSecondary)).append(";\n");
        
        css.append("}\n");
        return css.toString();
    }
}
