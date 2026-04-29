package com.kairos.components;

import javafx.scene.control.TableView;

public class EnhancedTableView<S> extends TableView<S> {

    public EnhancedTableView() {
        super();
        setupUI();
    }

    private void setupUI() {
        getStyleClass().add("table-view");
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Setup row factory for custom styling per row if needed
        setRowFactory(tv -> {
            javafx.scene.control.TableRow<S> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    S rowData = row.getItem();
                    System.out.println("Double clicked on: " + rowData);
                    // Emit edit event
                }
            });
            return row;
        });
    }
}
