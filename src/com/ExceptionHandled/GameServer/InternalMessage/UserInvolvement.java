package com.ExceptionHandled.GameServer.InternalMessage;

import java.util.List;

public class UserInvolvement {
    private List<String> gamesPlaying;
    private List<String> gamesViewing;

    public UserInvolvement(List<String> gamesPlaying, List<String> gamesViewing) {
        this.gamesPlaying = gamesPlaying;
        this.gamesViewing = gamesViewing;
    }

    public List<String> getGamesPlaying() {
        return gamesPlaying;
    }

    public List<String> getGamesViewing() {
        return gamesViewing;
    }
}
