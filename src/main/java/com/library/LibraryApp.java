package com.library;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX {@link Application} subclass for the Library Management System.
 *
 * <p>Responsible for loading the main FXML layout, attaching the global
 * stylesheet, and configuring the primary {@link Stage}.
 *
 * <p>Separated from {@link AppLauncher} (and from {@link Main}) to avoid
 * module-path issues: the JVM requires that the class that calls
 * {@link Application#launch} is not itself an {@link Application} subclass
 * when the module path is used.
 *
 * @see AppLauncher
 * @see Main
 */
public class LibraryApp extends Application {

    /**
     * Initializes and shows the primary application window.
     *
     * <p>Loads {@code /fxml/MainView.fxml} (which wires up
     * {@link com.library.controller.MainController}) and applies
     * {@code /css/style.css}.
     *
     * @param stage the primary stage provided by the JavaFX runtime
     * @throws IOException if the FXML or CSS resource cannot be loaded
     */
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