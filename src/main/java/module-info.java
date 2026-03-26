module com.library {
    // JavaFX — підключимо на наступному кроці (зараз тільки консоль)
    requires javafx.controls;
    requires javafx.fxml;

    // PostgreSQL JDBC driver
    requires java.sql;

    //.env
    requires io.github.cdimascio.dotenv.java;
}