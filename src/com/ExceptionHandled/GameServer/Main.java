package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameServer.Database.SQLiteQuery;
import com.ExceptionHandled.GameServer.ServerUI.Lobby.LobbyController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Server gameServer = new Server();

        FXMLLoader lobbyLoader = new FXMLLoader(getClass().getResource("ServerUI/Lobby/Lobby.fxml"));
        Parent lobby = lobbyLoader.load();
        LobbyController lbc = lobbyLoader.getController();
        lbc.setServer(gameServer);

        stage.setTitle("Server");
        stage.setScene(new Scene(lobby));
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                System.exit(2);
            }
        });

        gameServer.addGameLogicObserver(lbc);
    }
}

