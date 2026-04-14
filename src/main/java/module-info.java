module com.library {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // Java SQL
    requires java.sql;

    // dotenv (Step 2)
    requires io.github.cdimascio.dotenv.java;

    // Allow FXMLLoader to access controller classes via reflection
    opens com.library               to javafx.fxml;
    opens com.library.controller    to javafx.fxml;
    opens com.library.model         to javafx.base;

    exports com.library;
}