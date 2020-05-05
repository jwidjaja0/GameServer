package com.ExceptionHandled.GameServer.ServerUI.Lobby;

import com.ExceptionHandled.GameMessages.MainMenu.ActiveGameHeader;
import com.ExceptionHandled.GameMessages.MainMenu.ListActiveGames;
import com.ExceptionHandled.GameServer.Observer.GameLogicObserver;
import com.ExceptionHandled.GameServer.Observer.GameLogicSubject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.util.List;

public class LobbyController implements GameLogicObserver {
    @FXML
    ListView activePlayerListView;

    @FXML
    ListView activeGamesListView;

    public LobbyController() {
    }

    public void initialize(){
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
    }
}
