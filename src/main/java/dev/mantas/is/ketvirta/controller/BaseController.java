package dev.mantas.is.ketvirta.controller;

import dev.mantas.is.ketvirta.Application;
import dev.mantas.is.ketvirta.model.database.DatabaseEntry;
import dev.mantas.is.ketvirta.util.InnerPasswordCodec;
import dev.mantas.is.ketvirta.util.SceneUtil;
import dev.mantas.is.ketvirta.util.UIHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.stage.WindowEvent;
import lombok.Getter;

import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BaseController implements Initializable {

    private static final String OBFUSCATED_PASSWORD = "●●●●●●●●";

    private final ObservableList<PasswordEntryView> entriesData = FXCollections.observableArrayList();
    private final ObservableList<PasswordEntryView> displayedEntriesData = FXCollections.observableArrayList();

    @FXML
    private Button addEntryBtn;
    @FXML
    private Button saveBtn;

    @FXML
    private TableView<PasswordEntryView> contentTable;

    @FXML
    private TableColumn<?, ?> titleColumn;
    @FXML
    private TableColumn<?, ?> usernameColumn;
    @FXML
    private TableColumn<PasswordEntryView, String> passwordColumn;
    @FXML
    private TableColumn<?, ?> actionsColumn;

    @FXML
    private TextField searchField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addEntryBtn.setOnAction(event -> {
            SceneUtil.openModal("entry-editor-view.fxml", EntryEditorController.class, (controller, stage) -> {
                stage.setTitle("Create");
                stage.setResizable(false);

                Platform.runLater(() -> stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_HIDDEN, e -> updateEntries()));
            });
        });

        searchField.setOnKeyTyped(event -> {
            updateDisplayedEntries();
        });

        saveBtn.setOnAction(event -> {
            try {
                saveBtn.setDisable(true);
                saveBtn.setText("Saving...");
                Application.saveDatabase();
                new Alert(Alert.AlertType.INFORMATION, "Database saved").showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Could not save database").showAndWait();
            } finally {
                saveBtn.setDisable(false);
                saveBtn.setText("Save");
            }
        });

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setStyle("-fx-alignment: CENTER;");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setStyle("-fx-alignment: CENTER;");
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        passwordColumn.setStyle("-fx-alignment: CENTER;");
        actionsColumn.setCellValueFactory(new PropertyValueFactory<>("actions"));
        actionsColumn.setStyle("-fx-alignment: CENTER;");

        contentTable.setItems(displayedEntriesData);

        Arrays.asList(titleColumn, usernameColumn, passwordColumn).forEach(column -> {
            UIHelper.addTooltipToColumnCells((TableColumn<PasswordEntryView, Object>) column,
                    (data) -> data == null ? null : data.description);
        });

        updateEntries();
    }

    @Getter
    public static class PasswordEntryView {
        private DatabaseEntry entry;

        private String title;
        private String username;
        private String password;
        private String description;
        private HBox actions;

        public PasswordEntryView(DatabaseEntry entry, HBox actions) {
            this.entry = entry;
            this.title = entry.getTitle();
            this.description = entry.getDescription();
            this.username = entry.getUsername();
            this.password = OBFUSCATED_PASSWORD;
            this.actions = actions;
        }

        public boolean _stateViewingPassword;
        public int _stateIndex;
    }

    private PasswordEntryView createPasswordEntryView(DatabaseEntry entry) {
        HBox actionsContainer = new HBox();
        actionsContainer.setSpacing(3D);

        PasswordEntryView entryView = new PasswordEntryView(entry, actionsContainer);

        Button copyBtn = new Button("Copy");
        copyBtn.setOnAction(event -> {
            try {
                ClipboardContent content = new ClipboardContent();
                content.putString(InnerPasswordCodec.decrypt(entry.getPassword()));
                Clipboard.getSystemClipboard().setContent(content);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(event -> {
            SceneUtil.openModal("entry-editor-view.fxml", EntryEditorController.class, (controller, stage) -> {
                stage.setTitle("Edit");
                stage.setResizable(false);
                controller.editEntry(entry);

                Platform.runLater(() -> stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_HIDDEN, e -> updateEntries()));
            });
        });

        Button viewBtn = new Button("View");
        viewBtn.setOnAction(event -> {
            if (entryView._stateViewingPassword) {
                entryView.password = OBFUSCATED_PASSWORD;
            } else {
                try {
                    entryView.password = InnerPasswordCodec.decrypt(entry.getPassword());
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }

            entryView._stateViewingPassword = !entryView._stateViewingPassword;
            entriesData.set(entryView._stateIndex, entryView);
            updateDisplayedEntries();
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #ff6347;");

        deleteBtn.setOnAction(event -> {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Delete entry '" + entry.getTitle() + "'?");
            Optional<ButtonType> result = confirmation.showAndWait();

            result.ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    Application.getDatabaseEntryManager().unregisterEntry(entry);
                    updateEntries();

                    new Alert(Alert.AlertType.INFORMATION, "Entry '" + entry.getTitle() + "' removed").show();
                }
            });
        });

        actionsContainer.getChildren().setAll(copyBtn, editBtn, viewBtn, deleteBtn);

        return entryView;
    }

    private void updateEntries() {
        entriesData.setAll(Application.getDatabaseEntryManager().getEntries().stream().map(this::createPasswordEntryView).collect(Collectors.toList()));
        updateDisplayedEntries();
    }

    private void updateDisplayedEntries() {
        String text = searchField.getText();

        if (text.isBlank()) {
            displayedEntriesData.setAll(entriesData);
            updateIndices();
            return;
        }

        boolean byUsername = text.startsWith("u:");
        String[] split = (byUsername ? text.substring(2) : text).split(" ");

        if (text.isBlank()) {
            displayedEntriesData.setAll(entriesData);
            updateIndices();
            return;
        }

        displayedEntriesData.setAll(entriesData.stream().filter(entry -> {
            String targetLc = (byUsername ? entry.username : entry.title).toLowerCase(Locale.ROOT);
            for (String part : split) {
                if (targetLc.contains(part.toLowerCase(Locale.ROOT))) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList()));

        updateIndices();
    }

    private void updateIndices() {
        int index = 0;
        for (PasswordEntryView entry : entriesData) {
            entry._stateIndex = index++;
        }
    }

}