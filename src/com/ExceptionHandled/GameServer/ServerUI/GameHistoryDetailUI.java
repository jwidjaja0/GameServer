package com.ExceptionHandled.GameServer.ServerUI;

import com.ExceptionHandled.GameMessages.Game.MoveValid;
import com.ExceptionHandled.GameMessages.Stats.GameHistoryDetail;
import com.ExceptionHandled.GameMessages.Stats.GameHistorySummary;
import com.ExceptionHandled.GameMessages.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class GameHistoryDetailUI {
    private GameHistorySummary gameHistorySummary;
    private List<MoveValid> moveValids;
    private List<MoveValidUI> moveValidUIList;
    private List<UserInfo> viewersInfo;

    public GameHistoryDetailUI(GameHistorySummary gameHistorySummary, List<MoveValid> moveValids, List<UserInfo> viewersInfo) {
        this.gameHistorySummary = gameHistorySummary;
        this.moveValids = moveValids;
        this.viewersInfo = viewersInfo;
    }

    public GameHistoryDetailUI(GameHistoryDetail history){
        this.gameHistorySummary = history.getGameHistorySummary();
        this.moveValids = history.getMoveMadeList();
        this.viewersInfo = history.getViewersInfo();

        moveValidUIList = new ArrayList<>();
        for(MoveValid mv : moveValids){
            MoveValidUI mvUI = new MoveValidUI(mv);
            moveValidUIList.add(mvUI);
        }
    }

    public GameHistorySummary getGameHistorySummary() {
        return gameHistorySummary;
    }

    public void setGameHistorySummary(GameHistorySummary gameHistorySummary) {
        this.gameHistorySummary = gameHistorySummary;
    }

    public List<MoveValid> getMoveValids() {
        return moveValids;
    }

    public void setMoveValids(List<MoveValid> moveValids) {
        this.moveValids = moveValids;
    }

    public List<MoveValidUI> getMoveValidUIList() {
        return moveValidUIList;
    }

    public List<UserInfo> getViewersInfo() {
        return viewersInfo;
    }

    public void setViewersInfo(List<UserInfo> viewersInfo) {
        this.viewersInfo = viewersInfo;
    }
}
