package com.ExceptionHandled.GameServer.ServerUI;

import com.ExceptionHandled.GameMessages.Game.MoveValid;

import java.util.Date;

public class MoveValidUI {
    private String gameID;
    private String player;
    private String xcoord;
    private String ycoord;
    private Date date;

    public MoveValidUI(MoveValid mv) {
        gameID = mv.getGameID();
        player = mv.getPlayer();
        xcoord = String.valueOf(mv.getxCoord());
        ycoord = String.valueOf(mv.getyCoord());
        date = mv.getDate();
    }

    public String getGameID() {
        return gameID;
    }

    public String getPlayer() {
        return player;
    }

    public String getXcoord() {
        return xcoord;
    }

    public String getYcoord() {
        return ycoord;
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

    public void setXcoord(String xcoord) {
        this.xcoord = xcoord;
    }

    public void setYcoord(String ycoord) {
        this.ycoord = ycoord;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
