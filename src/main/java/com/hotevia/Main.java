package com.hotevia;

import com.hotevia.config.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.util.logging.Logger;

/**
 * Main application entry point for Hotevia.
 * Smart Hotel Reservation &amp; Management System
 */
public class Main extends Application {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    @Override
    public void start(Stage primaryStage) {
        try {
            // Attempt non-blocking DB initialization check
            new Thread(() -> {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    if (conn != null) {
                        LOGGER.info("Database initialized successfully at application startup.");
                    }
                } catch (Exception e) {
                    LOGGER.warning("Startup database check note: " + e.getMessage() 
                            + ". Make sure MySQL is running on localhost:3306 or configure connection settings.");
                }
            }).start();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 950, 650);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

            primaryStage.setTitle("Hotevia - Smart Hotel Reservation & Management System");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (Exception e) {
            LOGGER.severe("Error starting Hotevia application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        DatabaseConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
