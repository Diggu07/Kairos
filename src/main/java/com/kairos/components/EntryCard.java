package com.kairos.components;

import com.kairos.model.Entry;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class EntryCard extends VBox {
    private Entry entry;

    public EntryCard(Entry entry) {
        this.entry = entry;
        setupUI();
        setupHoverEffects();
    }

    private void setupUI() {
        setPadding(new Insets(16));
        setSpacing(8);
        setStyle("-fx-background-color: -color-surface; -fx-background-radius: 12; -fx-border-color: -color-border-custom; -fx-border-radius: 12; -fx-border-width: 1;");
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(6);
        shadow.setOffsetY(4);
        setEffect(shadow);

        Label titleLabel = new Label(entry.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: -color-text-primary;");

        Label contentLabel = new Label(entry.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setMaxHeight(40);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-text-secondary;");

        HBox tagsBox = new HBox(5);
        if (entry.getTags() != null && !entry.getTags().isEmpty()) {
            String[] tags = entry.getTags().split(",");
            for (String tag : tags) {
                Label tagLabel = new Label(tag.trim());
                tagLabel.setStyle("-fx-background-color: -color-primary-50; -fx-text-fill: -color-primary; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 12px;");
                tagsBox.getChildren().add(tagLabel);
            }
        }

        HBox metaBox = new HBox(15);
        metaBox.setStyle("-fx-font-size: 12px; -fx-text-fill: -color-text-tertiary;");
        
        Label typeLabel = new Label("Type: " + entry.getType());
        Label priorityLabel = new Label("Priority: " + entry.getPriority());
        metaBox.getChildren().addAll(typeLabel, priorityLabel);

        getChildren().addAll(titleLabel, contentLabel, tagsBox, metaBox);
    }

    private void setupHoverEffects() {
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), this);
        TranslateTransition translate = new TranslateTransition(Duration.millis(200), this);
        
        DropShadow hoverShadow = new DropShadow();
        hoverShadow.setColor(Color.rgb(0, 0, 0, 0.15));
        hoverShadow.setRadius(15);
        hoverShadow.setOffsetY(10);

        DropShadow normalShadow = (DropShadow) getEffect();

        setOnMouseEntered(e -> {
            scale.setToX(1.01);
            scale.setToY(1.01);
            translate.setToY(-2);
            setEffect(hoverShadow);
            setStyle("-fx-background-color: -color-surface; -fx-background-radius: 12; -fx-border-color: -color-primary; -fx-border-radius: 12; -fx-border-width: 1;");
            scale.playFromStart();
            translate.playFromStart();
        });

        setOnMouseExited(e -> {
            scale.setToX(1.0);
            scale.setToY(1.0);
            translate.setToY(0);
            setEffect(normalShadow);
            setStyle("-fx-background-color: -color-surface; -fx-background-radius: 12; -fx-border-color: -color-border-custom; -fx-border-radius: 12; -fx-border-width: 1;");
            scale.playFromStart();
            translate.playFromStart();
        });
    }
}
