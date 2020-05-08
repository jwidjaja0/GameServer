package com.ExceptionHandled.GameServer.ServerUI.Lobby;

import com.ExceptionHandled.GameMessages.Game.MoveValid;
import com.ExceptionHandled.GameMessages.Stats.GameHistoryDetail;
import com.ExceptionHandled.GameMessages.Stats.GameHistorySummary;
import com.ExceptionHandled.GameMessages.UserInfo;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GameDetailController {
    @FXML Text gameIDText;
    @FXML Text gameNameText;
    @FXML Text player1Text;
    @FXML Text player2Text;
    @FXML Text matchResultText;
    @FXML Text startTimeText;
    @FXML Text endTimeText;

    @FXML TableView<MoveValid> moveHistoryView;
    @FXML TableColumn<MoveValid,String> playerCol;
    @FXML TableColumn<MoveValid, Integer> xCol;
    @FXML TableColumn<MoveValid, Integer> yCol;
    @FXML TableColumn<MoveValid, Date> timeCol;

    @FXML TableView<UserInfo> viewersView;
    @FXML TableColumn<UserInfo,String> vidCol;
    @FXML TableColumn<UserInfo,String> userCol;
    @FXML TableColumn<UserInfo,String> fNameCol;
    @FXML TableColumn<UserInfo,String> lNameCol;


    GameHistoryDetail gameHistoryDetail;

    public GameDetailController() {
    }

    public void initialize(){
        playerCol.setCellValueFactory(new PropertyValueFactory<>("player"));
        xCol.setCellValueFactory(new PropertyValueFactory<>("xCoord"));

        //TODO: fix this to be able to set int
        xCol.setCellFactory(new Callback<TableColumn<MoveValid, Integer>, TableCell<MoveValid, Integer>>() {
            @Override
            public TableCell<MoveValid, Integer> call(TableColumn<MoveValid, Integer> moveValidIntegerTableColumn) {
                return null;
            }
        });


        yCol.setCellValueFactory(new PropertyValueFactory<>("yCoord"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        vidCol.setCellValueFactory(new PropertyValueFactory<>("userID"));
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        fNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
    }

    public void setGameHistoryDetail(GameHistoryDetail gameHistoryDetail) {
        this.gameHistoryDetail = gameHistoryDetail;
    }

    public void setInfo(){
        GameHistorySummary summary = gameHistoryDetail.getGameHistorySummary();
        gameIDText.setText(summary.getGameID());
        gameNameText.setText(summary.getGameName());
        player1Text.setText(summary.getPlayer1());
        player2Text.setText(summary.getPlayer2());
        matchResultText.setText(summary.getMatchResult());
        startTimeText.setText(summary.getStartDate().toString());
        if(summary.getEndDate().getTime() < summary.getStartDate().getTime()){
            endTimeText.setText("-");
        }
        else{
            endTimeText.setText(summary.getEndDate().toString());
        }

        populateMoveList(gameHistoryDetail.getMoveMadeList());
        populateViewers(gameHistoryDetail.getViewersInfo());
    }

    private void populateMoveList(List<MoveValid> moveList){
        moveHistoryView.getItems().addAll(moveList);
        List<Integer> xCoord = new ArrayList<>();
        List<Integer> yCoord = new ArrayList<>();

        for(int i = 0; i < moveList.size(); i++){
            xCoord.add(moveList.get(i).getxCoord());
            yCoord.add(moveList.get(i).getyCoord());
        }
        ObservableList<Integer> xList = FXCollections.observableArrayList();


    }

    private void populateViewers(List<UserInfo> viewerList){
        viewersView.getItems().addAll(viewerList);
    }
}
