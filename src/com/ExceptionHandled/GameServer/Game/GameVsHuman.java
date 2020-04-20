package com.ExceptionHandled.GameServer.Game;

import com.ExceptionHandled.GameMessages.Game.MoveMade;

public class GameVsHuman extends Game {

    public GameVsHuman() {
    }

    @Override
    public void makeTurn(MoveMade moveMade) {
        setMove(moveMade.getxCoord(), moveMade.getyCoord(), getTurnToken());
    }
}

