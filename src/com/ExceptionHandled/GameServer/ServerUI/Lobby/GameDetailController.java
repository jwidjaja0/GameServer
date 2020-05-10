package com.ExceptionHandled.GameServer.ServerUI.Lobby;

import com.ExceptionHandled.GameMessages.Game.MoveValid;
import com.ExceptionHandled.GameMessages.Stats.GameHistoryDetail;
import com.ExceptionHandled.GameMessages.Stats.GameHistorySummary;
import com.ExceptionHandled.GameMessages.UserInfo;
import com.ExceptionHandled.GameServer.ServerUI.GameHistoryDetailUI;
import com.ExceptionHandled.GameServer.ServerUI.MoveValidUI;
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

    @FXML  TableView<MoveValidUI> moveHistoryView;
    @FXML  TableColumn<MoveValidUI, String> playerCol;
    @FXML  TableColumn<MoveValidUI, String> xCol;
    @FXML  TableColumn<MoveValidUI, String> yCol;
    @FXML  TableColumn<MoveValidUI, Date> timeCol;

    @FXML private TableView<UserInfo> viewersView;
    @FXML private TableColumn<UserInfo,String> vidCol;
    @FXML private TableColumn<UserInfo,String> userCol;
    @FXML private TableColumn<UserInfo,String> fNameCol;
    @FXML private TableColumn<UserInfo,String> lNameCol;


    GameHistoryDetailUI gameHistoryDetailUI;

    public void setGameHistoryDetailUI(GameHistoryDetailUI gameHistoryDetailUI) {
        this.gameHistoryDetailUI = gameHistoryDetailUI;
    }

    public GameDetailController() {
    }

    public void initialize(){
        playerCol.setCellValueFactory(new PropertyValueFactory<>("player"));
        xCol.setCellValueFactory(new PropertyValueFactory<>("xcoord"));
        yCol.setCellValueFactory(new PropertyValueFactory<>("ycoord"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        vidCol.setCellValueFactory(new PropertyValueFactory<>("userID"));
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        fNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
    }


    public void setInfo(){
        GameHistorySummary summary = gameHistoryDetailUI.getGameHistorySummary();
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

        populateMoveList(gameHistoryDetailUI.getMoveValidUIList());
        populateViewers(gameHistoryDetailUI.getViewersInfo());
    }

    private void populateMoveList(List<MoveValidUI> moveList){
        moveHistoryView.getItems().addAll(moveList);
    }

    private void populateViewers(List<UserInfo> viewerList){
        viewersView.getItems().addAll(viewerList);
    }
}
