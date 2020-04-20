package com.ExceptionHandled.GameServer.Game;

import java.io.IOException;
import com.ExceptionHandled.GameMessages.Game.MoveMade;

public class GameVsAI extends Game {
    private AIPlayer aiPlayer;

    public GameVsAI() throws IOException {
        aiPlayer = new AIPlayer();
    }

    @Override
    public void reset() {
        super.reset();
        if(getWhoseTurn()){
            aiMove();
        }
    }

    //let the user and ai both make a move
    @Override
    public void makeTurn(MoveMade moveMade) {
        if (getWhoseTurn()) {
            setMove(moveMade.getxCoord(), moveMade.getyCoord(), getTurnToken());
            switchTurn();
        }
        //otherwise get the AI's move and switch turns.  also check to make sure the game isn't over
        else {
            setMove(moveMade.getxCoord(), moveMade.getyCoord(), getTurnToken());
            if (!gameOver()){
                switchTurn();
                aiMove();
            }
        }
    }

    //update the AI's board and ask for its move.  then notify observers
    public void aiMove(){
        aiPlayer.updateBoard(getBoard());
        int[] move = aiPlayer.makeMove();
        setMove(move[1], move[2], getTurnToken());
    }
}

