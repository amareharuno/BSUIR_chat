package by.bsuir.rpnJava.lr5.server;

import by.bsuir.rpnJava.lr5.server.util.Connection;
import by.bsuir.rpnJava.lr5.server.util.InputUtil;
import by.bsuir.rpnJava.lr5.server.util.Message;
import by.bsuir.rpnJava.lr5.server.util.MessageType;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server extends Application {
    @FXML
    private TextField portLabel;
    @FXML
    private TextArea text;

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("server.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Server");
        primaryStage.show();
    }

    public void startUpServer() {
        int portNumber = Integer.parseInt(portLabel.getText());
        try (ServerSocket socket = new ServerSocket(portNumber)) {
            text.setText("Сервер запущен");
            while (true) {
                new Handler(socket.accept()).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Handler extends Thread {
        private Socket socket;
        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            InputUtil.writeMessage("Установлено новое соединение с удаленным адресом " + socket.getRemoteSocketAddress());
            String userName = "";
            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                sendListOfUsers(connection, userName);
                serverMainLoop(connection, userName);
            } catch (IOException e) {
                InputUtil.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");
            } catch (ClassNotFoundException e) {
                InputUtil.writeMessage("Произошла ошибка при обмене данными с удаленным адресом (класс не найден)");
            } finally {
                if (!userName.equals("")) {
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                }
            }
            InputUtil.writeMessage("Соединение с удаленным адресом закрыто");
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message receivedMessage = connection.receive();
                if (receivedMessage.getType().equals(MessageType.USER_NAME) && !receivedMessage.getData().isEmpty()
                        && receivedMessage.getData() != null && !connectionMap.containsKey(receivedMessage.getData())) {
                    connectionMap.put(receivedMessage.getData(), connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED));
                    return receivedMessage.getData();
                }
            }
        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
                if (!entry.getKey().equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, entry.getKey()));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    String text = userName + ": " + message.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, text));
                } else {
                    InputUtil.writeMessage("This message don't TEXT");
                }
            }
        }
    }

    public static void sendBroadcastMessage(Message message) {
        try {
            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
                entry.getValue().send(message);
            }
        } catch (IOException e) {
            InputUtil.writeMessage("Ошибка при отправке сообщения");
        }
    }
}
