package dev.mantas.is.ketvirta.util;

import dev.mantas.is.ketvirta.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SceneUtil {

    public static boolean load(String resource) {
        FXMLLoader loader = new FXMLLoader(Application.class.getResource(resource));
        Parent nextViewRoot;

        try {
            nextViewRoot = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        Stage stage = Application.getPrimaryStage();
        Scene nextScene = new Scene(nextViewRoot);
        stage.setScene(nextScene);
        stage.show();

        return true;
    }

    public static <C> boolean load(String resource, Class<C> type, Consumer<C> controllerModifier) {
        FXMLLoader loader = new FXMLLoader(Application.class.getResource(resource));
        Parent nextViewRoot;

        try {
            nextViewRoot = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        C controller = loader.getController();
        controllerModifier.accept(controller);
        Stage stage = Application.getPrimaryStage();
        Scene nextScene = new Scene(nextViewRoot);
        stage.setScene(nextScene);
        stage.show();

        return true;
    }

    public static <C> Optional<Stage> openModal(String resource, Class<C> type, BiConsumer<C, Stage> controllerModifier) {
        FXMLLoader loader = new FXMLLoader(Application.class.getResource(resource));
        Parent viewRoot;

        try {
            viewRoot = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }

        C controller = loader.getController();
        Stage stage = new Stage();
        Scene scene = new Scene(viewRoot);

        controllerModifier.accept(controller, stage);

        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        return Optional.of(stage);
    }

}
