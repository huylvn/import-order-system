package com.importorder;

import com.importorder.context.ApplicationContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * JavaFX application entry point.
 */
public class MainApp extends Application {

    private static final String MAIN_LAYOUT_FXML = "/fxml/MainLayout.fxml";
    private static final String APP_CSS = "/css/app.css";
    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 800;

    @Override
    public void init() {
        ApplicationContext.initialize();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_LAYOUT_FXML));
        Parent root = loader.load();

        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource(APP_CSS)).toExternalForm());

        primaryStage.setTitle("Hệ thống phân bổ đơn đặt hàng nhập khẩu");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
