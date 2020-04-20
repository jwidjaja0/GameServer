package com.ExceptionHandled.GameServer.Game;

import java.io.IOException;

public class GameVsAI extends Game {
    private AIPlayer aiPlayer;

    public GameVsAI() throws IOException {
        aiPlayer = new AIPlayer();
    }

    @Override
    public void reset() {
        super.reset();
        if(getTurn() == 'O'){
            aiMove();
        }
    }

    //let the user and ai both make a move
    @Override
    public void makeTurn(Move move) {
        //let the observers know and change as needed
        setChanged();
        //if its player2's turn then use the user's move and switch turn
        if (getWhoseTurn()) {
            setMove(move.getRow(), move.getCol(), getTurnToken());
            switchTurn();
        }
        //otherwise get the AI's move and switch turns.  also check to make sure the game isn't over
        else {
            setMove(move.getRow(), move.getCol(), getTurnToken());
            if (!gameOver()){
                switchTurn();
                aiMove();
            }
        }
    }

    //update the AI's board and ask for its move.  then notify observers
    public void aiMove(){
        aiPlayer.updateBoard(getBoard());
        setChanged();
        notifyObservers(aiPlayer.makeMove());
    }
}

