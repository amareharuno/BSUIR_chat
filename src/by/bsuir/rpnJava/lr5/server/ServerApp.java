package by.bsuir.rpnJava.lr5.server;

import by.bsuir.rpnJava.lr5.Server;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ServerApp extends Application {
    @FXML
    private TextField portLabel;
    @FXML
    private TextArea text;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("server.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle("ServerApp");
        primaryStage.show();
    }

    public void startUpServer() {
        this.text.setText("Server started");
        new Server();
    }
}
