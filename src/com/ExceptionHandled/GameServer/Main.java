package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameServer.Database.SQLiteQuery;
import com.ExceptionHandled.GameServer.ServerUI.Lobby.LobbyController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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

        stage.setTitle("My app");
        stage.setScene(new Scene(lobby));
        stage.show();

        gameServer.addGameLogicObserver(lbc);
    }
}

