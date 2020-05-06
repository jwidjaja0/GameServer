package com.ExceptionHandled.GameServer.ServerUI.Lobby;

import com.ExceptionHandled.GameMessages.Stats.GameHistoryDetail;
import com.ExceptionHandled.GameMessages.Stats.GameHistorySummary;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class GameDetailController {
    @FXML Text gameIDText;
    @FXML Text gameNameText;
    @FXML Text player1Text;
    @FXML Text player2Text;
    @FXML Text matchResultText;
    @FXML Text startTimeText;
    @FXML Text endTimeText;

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


    }
}
