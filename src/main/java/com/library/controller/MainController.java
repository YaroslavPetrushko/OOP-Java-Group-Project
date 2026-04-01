package com.library.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for main.fxml.
 *
 * Step 3 — skeleton only: shows a status label.
 * CRUD logic and table bindings will be added in Step 4.
 */
public class MainController {

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        statusLabel.setText("Connected to database ✅");
    }
}