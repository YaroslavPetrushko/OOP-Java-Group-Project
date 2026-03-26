package com.library;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * JavaFX Application class.
 *
 * Separated from Main to avoid module-path issues —
 * Application subclasses must not be the main() entry point.
 */
public class LibraryApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/MainView.fxml")
        );

        Scene scene = new Scene(loader.load(), 960, 640);
        scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
        );

        stage.setTitle("Library Management System");
        stage.setMinWidth(800);
        stage.setMinHeight(520);
        stage.setScene(scene);
        stage.show();
    }
}