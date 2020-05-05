package com.ExceptionHandled.GameServer.ServerUI.Lobby;

import com.ExceptionHandled.GameMessages.MainMenu.ActiveGameHeader;
import com.ExceptionHandled.GameMessages.MainMenu.ListActiveGames;
import com.ExceptionHandled.GameServer.InternalMessage.ActivePlayerList;
import com.ExceptionHandled.GameServer.Observer.GameLogicObserver;
import com.ExceptionHandled.GameServer.Observer.GameLogicSubject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.util.List;

public class LobbyController implements GameLogicObserver {
    @FXML
    ListView activePlayerListView;
    @FXML
    ListView activeGamesListView;
    @FXML
    Button playerButton;
    @FXML
    Button gameButton;


    public LobbyController() {
    }

    public void initialize(){
        playerButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

            }
        });
    }


    @Override
    public void update(GameLogicSubject s, Object arg) {
        if(arg instanceof ListActiveGames){
            List<ActiveGameHeader> games = ((ListActiveGames) arg).getActiveGameHeaderList();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    activeGamesListView.getItems().clear();
                    for(ActiveGameHeader h: games){
                        activeGamesListView.getItems().add(h);
                    }
                }
            });
        }
        if(arg instanceof ActivePlayerList){
            List<String> playerList = ((ActivePlayerList)arg).getPlayerList();
            Platform.runLater(new Runnable( ) {
                @Override
                public void run() {
                    activePlayerListView.getItems().clear();
                    for(String playerID : playerList){
                        activePlayerListView.getItems().add(playerID);
                    }
                }
            });
        }
    }
}
