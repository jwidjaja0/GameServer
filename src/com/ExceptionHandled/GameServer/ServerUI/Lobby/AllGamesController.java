package com.ExceptionHandled.GameServer.ServerUI.Lobby;

import com.ExceptionHandled.GameMessages.Stats.GameHistoryDetail;
import com.ExceptionHandled.GameMessages.Stats.GameHistorySummary;
import com.ExceptionHandled.GameServer.Database.SQLiteQuery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.Date;
import java.util.List;

public class AllGamesController {
    @FXML
    private TableView<GameHistorySummary> gameTableView;
    @FXML
    private TableColumn<GameHistorySummary, String> gameIDCol;
    @FXML private TableColumn<GameHistorySummary, String> gamenameCol;
    @FXML private TableColumn<GameHistorySummary, String> playerOneCol;
    @FXML private TableColumn<GameHistorySummary, String> playerTwoCol;
    @FXML private TableColumn<GameHistorySummary, String> matchCol;
    @FXML private TableColumn<GameHistorySummary, Date> startDateCol;
    @FXML private TableColumn<GameHistorySummary, Date> endDateCol;
    @FXML private Button detailButton;

    private List<GameHistorySummary> gameHistorySummaries;

    public void initialize(){
        gameIDCol.setCellValueFactory(new PropertyValueFactory<>("gameID"));
        gamenameCol.setCellValueFactory(new PropertyValueFactory<>("gameName"));
        playerOneCol.setCellValueFactory(new PropertyValueFactory<>("player1"));
        playerTwoCol.setCellValueFactory(new PropertyValueFactory<>("player2"));
        matchCol.setCellValueFactory(new PropertyValueFactory<>("matchResult"));
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        detailButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                GameHistorySummary summary = gameTableView.getSelectionModel().getSelectedItem();
                getGameDetail(summary.getGameID());

            }
        });
    }

    public void getGameDetail(String gameID){
        GameHistoryDetail detail = SQLiteQuery.getInstance().getGameDetail(gameID);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("GameDetail.fxml"));
        GameDetailController gdc;

        try {
            Parent root = loader.load();
            gdc = loader.getController();
            gdc.setGameHistoryDetail(detail);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
