package com.ExceptionHandled.GameServer.ServerUI.Lobby;

import com.ExceptionHandled.GameMessages.MainMenu.ActiveGameHeader;
import com.ExceptionHandled.GameMessages.MainMenu.ListActiveGames;
import com.ExceptionHandled.GameMessages.Stats.GameHistoryDetail;
import com.ExceptionHandled.GameServer.Database.SQLiteQuery;
import com.ExceptionHandled.GameServer.InternalMessage.ActivePlayerList;
import com.ExceptionHandled.GameServer.Observer.GameLogicObserver;
import com.ExceptionHandled.GameServer.Observer.GameLogicSubject;
import com.ExceptionHandled.GameServer.Player;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
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
    @FXML
    Button allPlayersButton;
    @FXML
    Button allGamesButton;


    public LobbyController() {
    }

    public void initialize(){
        playerButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                getPlayerDetail();
            }
        });

        gameButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

            }
        });
    }

    private void getPlayerDetail() {
        String playerID = (String)activePlayerListView.getSelectionModel().getSelectedItem();
        Player player = SQLiteQuery.getInstance().getPlayerDetail(playerID);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("PlayerDetail.fxml"));

        PlayerDetailController pdc;
        try {
            Parent root = loader.load();
            pdc = loader.getController();
            pdc.setFields(player);
            Stage stage = new Stage();
            stage.setTitle("Player Detail");
            stage.setScene(new Scene(root));

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getGameDetail(String gameID){
        GameHistoryDetail detail = SQLiteQuery.getInstance().getGameDetail(gameID);

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
