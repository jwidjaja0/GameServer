package com.ExceptionHandled.GameServer.Game;

import java.util.Observable;

public abstract class Game extends Observable{
    private TicTacToe ticTacToe;
    private boolean player2Turn;
    private char firstTurn;

    final char player1 = 'X';
    final char player2 = 'O';

    //create new board, let player1 start, add observer
    protected Game() {
        ticTacToe = new TicTacToe();
        player2Turn = false;
        firstTurn = player1;
    }

    //returns the winner if someone won, "D" for draw, or "-" if the game isn't over yet
    public char whoWon(){
        if (ticTacToe.isWon('X'))
            return 'X';
        else if (ticTacToe.isWon('O'))
            return 'O';
        else if (ticTacToe.isFull())
                return 'D';
        return '-';
    }

    protected void setMove(int row, int col, char token){
        ticTacToe.setMove(row, col, token);
    }

    protected boolean getWhoseTurn(){
        return player2Turn;
    }

    protected void switchTurn(){
        player2Turn = !player2Turn;
    }

    protected char getTurnToken(){
        if (player2Turn)
            return player2;
        return player1;
    }

    protected char getTurn(){
        if (player2Turn)
            return player2;
        else
            return player1;
    }

    protected boolean gameOver(){
        return ticTacToe.isGameOver();
    }

    protected TicTacToe getBoard(){
        return ticTacToe;
    }

    public void reset(){
        char first = firstTurn;
        ticTacToe = new TicTacToe();
        if (first == player1){
            player2Turn = true;
            firstTurn = player2;
        }
        else {
            player2Turn = false;
            firstTurn = player1;
        }
    }

    public abstract void makeTurn(Move move);
}
