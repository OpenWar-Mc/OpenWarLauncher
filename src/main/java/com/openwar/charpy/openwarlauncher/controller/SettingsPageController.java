package com.openwar.charpy.openwarlauncher.controller;

import com.openwar.charpy.openwarlauncher.utils.SettingsManager;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SettingsPageController {

    @FXML private ComboBox<Integer> gbComboBox;
    @FXML private TextField widthField;
    @FXML private TextField heightField;

    private final SettingsManager settingsManager = SettingsManager.getInstance();

    @FXML
    public void initialize() {
        setupGBOptions();
        loadCurrentSettings();
    }

    private void setupGBOptions() {
        gbComboBox.getItems().addAll(2, 4, 6, 8, 16);
    }

    private void loadCurrentSettings() {
        gbComboBox.setValue(settingsManager.getGb());
        widthField.setText(String.valueOf(settingsManager.getWidth()));
        heightField.setText(String.valueOf(settingsManager.getHeight()));
    }

    @FXML
    private void handleSave() {
        try {
            settingsManager.setGb(gbComboBox.getValue());
            settingsManager.setWidth(Integer.parseInt(widthField.getText()));
            settingsManager.setHeight(Integer.parseInt(heightField.getText()));
            settingsManager.saveSettings();
            closeWindow();
        } catch (NumberFormatException e) {
            System.err.println("Format de nombre invalide !");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) gbComboBox.getScene().getWindow();
        stage.close();
    }
}