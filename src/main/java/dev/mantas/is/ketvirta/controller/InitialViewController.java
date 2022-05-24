package dev.mantas.is.ketvirta.controller;

import dev.mantas.is.ketvirta.Application;
import dev.mantas.is.ketvirta.model.InitializationDelegate;
import dev.mantas.is.ketvirta.util.InnerPasswordCodec;
import dev.mantas.is.ketvirta.util.SceneUtil;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import lombok.Setter;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class InitialViewController implements Initializable {

    @FXML
    private Label statusLabel;
    @FXML
    private TextField databasePathField;
    @FXML
    private Button databasePathSelectBtn;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button viewPasswordBtn;
    @FXML
    private Button unlockBtn;

    @Setter
    private InitializationDelegate initializationDelegate;

    private boolean viewingPassword;
    private ChangeListener<String> passwordChangeListener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusLabel.setText("");

        databasePathField.setText(new File("./database.csv").getAbsolutePath());

        databasePathSelectBtn.setOnAction(event -> {
            FileChooser chooser = new FileChooser();

            chooser.setInitialDirectory(new File("./"));
            chooser.setTitle("Choose a database file");

            File selected = chooser.showOpenDialog(null);

            if (selected != null) {
                databasePathField.setText(selected.getAbsolutePath());
            }
        });

        viewPasswordBtn.setOnAction(event -> {
            viewingPassword = !viewingPassword;

            if (viewingPassword) {
                Tooltip tooltip = new Tooltip();
                tooltip.setShowDelay(Duration.ZERO);
                tooltip.setAutoHide(false);
                tooltip.setText(passwordField.getText());

                passwordField.textProperty().addListener(passwordChangeListener = (observable, oldValue, newValue) -> {
                    tooltip.setText(passwordField.getText());
                });

                passwordField.setTooltip(tooltip);
            } else {
                passwordField.setTooltip(null);
                passwordField.textProperty().removeListener(passwordChangeListener);
                passwordChangeListener = null;
            }
        });

        unlockBtn.setOnAction(event -> {
            String databasePath = databasePathField.getText();
            String masterPassword = passwordField.getText();

            if (databasePath == null || databasePath.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Database path field must not be empty").showAndWait();
                return;
            }

            if (masterPassword == null || masterPassword.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Password field must not be empty").showAndWait();
                return;
            }

            initializationDelegate.setDatabasePath(databasePath);

            try {
                initializationDelegate.setMasterPassword(InnerPasswordCodec.encrypt(masterPassword));
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            if (Application.loadDatabase()) {
                SceneUtil.load("home-view.fxml");
            } else {
                new Alert(Alert.AlertType.ERROR, "Could not access selected database.").showAndWait();
            }
        });
    }

    public void setDatabaseFilePath(String path) {
        this.databasePathField.setText(path);
    }

}
