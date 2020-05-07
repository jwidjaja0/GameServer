package com.ExceptionHandled.GameServer.ServerUI.Lobby;

import com.ExceptionHandled.GameMessages.Stats.GameHistorySummary;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;


import java.util.Date;
import java.util.List;

public class AllGamesController {
    @FXML
    private TableView<GameHistorySummary> gameTableView;
    @FXML
    private TableColumn<GameHistorySummary, String> gameIDCol;
    @FXML private TableColumn<GameHistorySummary, String> gamenameCol;
    @FXML private TableColumn<GameHistorySummary,String> playerOneCol;
    @FXML private TableColumn<GameHistorySummary, String> playerTwoCol;
    @FXML private TableColumn<GameHistorySummary, String> matchCol;
    @FXML private TableColumn<GameHistorySummary, Date> startDateCol;
    @FXML private TableColumn<GameHistorySummary, Date> endDateCol;

    private List<GameHistorySummary> gameHistorySummaries;

    public void initialize(){
        gameIDCol.setCellValueFactory(new PropertyValueFactory<>("gameID"));
        gamenameCol.setCellValueFactory(new PropertyValueFactory<>("gameName"));
        playerOneCol.setCellValueFactory(new PropertyValueFactory<>("player1"));
        playerTwoCol.setCellValueFactory(new PropertyValueFactory<>("player2"));
        matchCol.setCellValueFactory(new PropertyValueFactory<>("matchResult"));
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
    }

    public void populate(){
        gameTableView.setItems(getGameInfo());
    }

    public ObservableList<GameHistorySummary> getGameInfo(){
        ObservableList<GameHistorySummary> gameHistoryList = FXCollections.observableArrayList();
        gameHistoryList.addAll(gameHistorySummaries);
        return gameHistoryList;
    }

    public void setGameHistorySummaries(List<GameHistorySummary> gameHistorySummaries) {
        this.gameHistorySummaries = gameHistorySummaries;
    }


}
