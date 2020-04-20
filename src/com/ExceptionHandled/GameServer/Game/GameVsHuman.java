package com.ExceptionHandled.GameServer.Game;

public class GameVsHuman extends Game {

    public GameVsHuman() {
    }

    @Override
    public void makeTurn(Move move) {
        setMove(move.getRow(), move.getCol(), getTurnToken());
    }
}

