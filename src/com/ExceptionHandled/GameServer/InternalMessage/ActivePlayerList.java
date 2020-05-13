package com.ExceptionHandled.GameServer.InternalMessage;

import java.util.List;

public class ActivePlayerList {
    private List<String> playerList;

    public ActivePlayerList(List<String> s) {
        playerList = s;
    }

    public List<String> getPlayerList() {
        return playerList;
    }
}
