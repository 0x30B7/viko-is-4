package dev.mantas.is.ketvirta;

import dev.mantas.is.ketvirta.controller.InitialViewController;
import dev.mantas.is.ketvirta.model.DatabaseEntryManager;
import dev.mantas.is.ketvirta.model.DatabaseLoader;
import dev.mantas.is.ketvirta.model.InitializationDelegate;
import dev.mantas.is.ketvirta.model.database.DatabaseReadResult;
import dev.mantas.is.ketvirta.model.database.DatabaseSerializer;
import dev.mantas.is.ketvirta.util.InnerPasswordCodec;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;

public class Application extends javafx.application.Application {

    private static Stage PRIMARY_STAGE;

    private static InnerPasswordCodec.InnerPassword MASTER_PASSWORD;
    private final static DatabaseLoader DATABASE_LOADER = new DatabaseLoader();
    private final static DatabaseEntryManager DATABASE_ENTRY_MANAGER = new DatabaseEntryManager();

    @Override
    public void start(Stage stage) throws Exception {
        PRIMARY_STAGE = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("initial-view.fxml"));
        Parent root = fxmlLoader.load();
        InitialViewController controller = fxmlLoader.getController();

        controller.setInitializationDelegate(new InitializationDelegate() {
            @Override
            public void setDatabasePath(String path) {
                DATABASE_LOADER.setDatabaseFilePath(Path.of(path));
            }

            @Override
            public void setMasterPassword(InnerPasswordCodec.InnerPassword password) {
                MASTER_PASSWORD = password;
            }
        });

        String lastPath = DATABASE_LOADER.checkPreviousPath();
        if (lastPath != null) {
            controller.setDatabaseFilePath(lastPath);
        }

        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("IS Ketvirta");
        stage.setScene(scene);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static Stage getPrimaryStage() {
        return PRIMARY_STAGE;
    }

    public static DatabaseEntryManager getDatabaseEntryManager() {
        return DATABASE_ENTRY_MANAGER;
    }

    public static boolean loadDatabase() {
        DatabaseReadResult result;

        try {
            result = DatabaseSerializer.deserialize(DATABASE_LOADER, MASTER_PASSWORD);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        if (!result.isSuccessful()) {
            result.exception().printStackTrace();
            return false;
        }

        DATABASE_ENTRY_MANAGER.updateEntries(result.contents());

        return true;
    }

    public static void saveDatabase() throws Exception {
        DatabaseSerializer.serialize(DATABASE_LOADER, DATABASE_ENTRY_MANAGER.getEntries(), MASTER_PASSWORD);
    }

}