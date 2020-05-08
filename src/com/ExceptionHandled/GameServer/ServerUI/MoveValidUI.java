package com.ExceptionHandled.GameServer.ServerUI;

import com.ExceptionHandled.GameMessages.Game.MoveValid;

import java.util.Date;

public class MoveValidUI {
    private String gameID;
    private String player;
    private Integer xCoord;
    private Integer yCoord;
    private Date date;

    public MoveValidUI(MoveValid mv) {
        gameID = mv.getGameID();
        player = mv.getPlayer();
        xCoord = mv.getxCoord();
        yCoord = mv.getyCoord();
        date = mv.getDate();
    }

    public String getGameID() {
        return gameID;
    }

    public String getPlayer() {
        return player;
    }

    public Integer getxCoord() {
        return xCoord;
    }

    public Integer getyCoord() {
        return yCoord;
    }

    public Date getDate() {
        return date;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public void setxCoord(Integer xCoord) {
        this.xCoord = xCoord;
    }

    public void setyCoord(Integer yCoord) {
        this.yCoord = yCoord;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
