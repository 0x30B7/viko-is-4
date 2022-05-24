package dev.mantas.is.ketvirta.controller;

import dev.mantas.is.ketvirta.Application;
import dev.mantas.is.ketvirta.model.database.DatabaseEntry;
import dev.mantas.is.ketvirta.util.InnerPasswordCodec;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class EntryEditorController implements Initializable {

    private InnerPasswordCodec.InnerPassword realPassword;

    @FXML
    private TextField titleField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button viewPasswordBtn;
    @FXML
    private Button generatePasswordBtn;
    @FXML
    private Button saveBtn;
    @FXML
    private Button cancelBtn;

    private boolean viewingPassword;
    private ChangeListener<String> passwordChangeListener;

    public void editEntry(DatabaseEntry toEdit) {
        titleField.setText(toEdit.getTitle());
        titleField.setEditable(false);
        titleField.setDisable(true);

        usernameField.setText(toEdit.getUsername());
        descriptionField.setText(toEdit.getDescription());

        try {
            passwordField.setText(InnerPasswordCodec.decrypt(toEdit.getPassword()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        generatePasswordBtn.setOnAction(event -> {
            passwordField.setText(generateRandomPassword());
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

        saveBtn.setOnAction(event -> {
            String title = titleField.getText();
            String username = usernameField.getText();
            String description = descriptionField.getText();

            if (title == null || title.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Title field must not be empty").showAndWait();
                return;
            }

            if (username == null || username.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Username field must not be empty").showAndWait();
                return;
            }

            if (passwordField.getText() == null || passwordField.getText().isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Password field must not be empty").showAndWait();
                return;
            }

            if (description == null)
                description = "";

            try {
                Application.getDatabaseEntryManager().registerEntry(new DatabaseEntry(
                        title, username, description, InnerPasswordCodec.encrypt(passwordField.getText())
                ));
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }


            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
        });

        cancelBtn.setOnAction(event -> {
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
        });
    }

    private String generateRandomPassword() {
        char[] alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+`~[];'\\,./{}:\"|<>?".toCharArray();
        Random random = new Random();
        char[] arr = new char[randomIntegerBetween(random, 32, 48)];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = alphabet[randomIntegerBetween(random, 0, alphabet.length - 1)];
        }

        return new String(arr);
    }

    private int randomIntegerBetween(Random random, int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

}
