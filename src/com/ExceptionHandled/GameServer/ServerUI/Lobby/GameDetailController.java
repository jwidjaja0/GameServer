package com.ExceptionHandled.GameServer.ServerUI.Lobby;

import com.ExceptionHandled.GameMessages.Game.MoveValid;
import com.ExceptionHandled.GameMessages.Stats.GameHistoryDetail;
import com.ExceptionHandled.GameMessages.Stats.GameHistorySummary;
import com.ExceptionHandled.GameMessages.UserInfo;


import javafx.beans.property.SimpleIntegerProperty;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GameDetailController {
    @FXML private Text gameIDText;
    @FXML private Text gameNameText;
    @FXML private Text player1Text;
    @FXML private Text player2Text;
    @FXML private Text matchResultText;
    @FXML private Text startTimeText;
    @FXML private Text endTimeText;

    @FXML  TableView<MoveValid> moveHistoryView;
    @FXML  TableColumn<MoveValid, String> playerCol;
    @FXML  TableColumn<MoveValid, String> xCol;
    @FXML  TableColumn<MoveValid, String> yCol;
    @FXML  TableColumn<MoveValid, Date> timeCol;

    @FXML private TableView<UserInfo> viewersView;
    @FXML private TableColumn<UserInfo,String> vidCol;
    @FXML private TableColumn<UserInfo,String> userCol;
    @FXML private TableColumn<UserInfo,String> fNameCol;
    @FXML private TableColumn<UserInfo,String> lNameCol;


    GameHistoryDetail gameHistoryDetail;

    public void setGameHistoryDetail(GameHistoryDetail gameHistoryDetail) {
        this.gameHistoryDetail = gameHistoryDetail;
        setInfo();
    }

    public GameDetailController() {
    }

    public void initialize(){
        playerCol.setCellValueFactory(new PropertyValueFactory<>("player"));
        //xCol.setCellValueFactory(new PropertyValueFactory<>("xcoord"));

        xCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MoveValid, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<MoveValid, String> moveValidStringCellDataFeatures) {
                Integer c = moveValidStringCellDataFeatures.getValue().getXCoord();
                return new SimpleStringProperty(c.toString());
            }
        });
        yCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MoveValid, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<MoveValid, String> moveValidStringCellDataFeatures) {
                Integer c = moveValidStringCellDataFeatures.getValue().getYCoord();
                return new SimpleStringProperty(c.toString());
            }
        });

//        yCol.setCellValueFactory(new PropertyValueFactory<>("ycoord"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        vidCol.setCellValueFactory(new PropertyValueFactory<>("userID"));
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        fNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
    }


    public void setInfo(){
        GameHistorySummary summary = gameHistoryDetail.getGameHistorySummary();
        gameIDText.setText(summary.getGameID());
        gameNameText.setText(summary.getGameName());
        player1Text.setText(summary.getPlayer1());
        player2Text.setText(summary.getPlayer2());
        matchResultText.setText(summary.getMatchResult());

        String pattern = "MM/dd/yyy HH:mm:ss";
        DateFormat df = new SimpleDateFormat(pattern);
        String startDateString = df.format(summary.getStartDate());
        String endDateString = df.format(summary.getEndDate());

        startTimeText.setText(startDateString); //TODO: figure out add hours if possible
        if(summary.getEndDate().getTime() < summary.getStartDate().getTime()){
            endTimeText.setText("-");
        }
        else{
            endTimeText.setText(endDateString);
        }

        populateMoveList(gameHistoryDetail.getMoveMadeList());
        populateViewers(gameHistoryDetail.getViewersInfo());
    }

    private void populateMoveList(List<MoveValid> moveList){
        moveHistoryView.getItems().addAll(moveList);
    }

    private void populateViewers(List<UserInfo> viewerList){
        viewersView.getItems().addAll(viewerList);
    }
}
