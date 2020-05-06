package com.ExceptionHandled.GameServer.ServerUI.Lobby;

import com.ExceptionHandled.GameMessages.Game.MoveValid;
import com.ExceptionHandled.GameMessages.Stats.GameHistoryDetail;
import com.ExceptionHandled.GameMessages.Stats.GameHistorySummary;
import com.ExceptionHandled.GameMessages.UserInfo;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Text;

import java.util.List;

public class GameDetailController {
    @FXML Text gameIDText;
    @FXML Text gameNameText;
    @FXML Text player1Text;
    @FXML Text player2Text;
    @FXML Text matchResultText;
    @FXML Text startTimeText;
    @FXML Text endTimeText;

    @FXML
    TableColumn playerCol;
    @FXML TableColumn xCol;
    @FXML TableColumn yCol;
    @FXML TableColumn timeCol;

    @FXML TableColumn vidCol;
    @FXML TableColumn userCol;
    @FXML TableColumn fNameCol;
    @FXML TableColumn lNameCol;


    GameHistoryDetail gameHistoryDetail;

    public GameDetailController() {
    }

    public void initialize(){}

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
        for(MoveValid mv : moveList){
            playerCol.getColumns().add(mv.getPlayer());
            xCol.getColumns().add(mv.getxCoord());
            yCol.getColumns().add(mv.getyCoord());
            timeCol.getColumns().add(mv.getUtilDate());
        }
    }

    private void populateViewers(List<UserInfo> viewerList){
        for(UserInfo info : viewerList){
            vidCol.getColumns().add(info.getUserID());
            userCol.getColumns().add(info.getUsername());
            fNameCol.getColumns().add(info.getFirstName());
            lNameCol.getColumns().add(info.getLastName());
        }

    }
}
