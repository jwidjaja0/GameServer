package com.ExceptionHandled.GameServer.Game;


public class Game {
    private TicTacToe ticTacToe;
    private boolean player2Turn;
    private char firstTurn;

    final char player1 = 'X';
    final char player2 = 'O';

    //create new board, let player1 start, add observer
    public Game() {
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

    public boolean validMove (int row, int col) {
        return ticTacToe.getCharAt(row, col) == ' ';
    }

    public void setMove(int row, int col, char token){
        ticTacToe.setMove(row, col, token);
    }

    public boolean getWhoseTurn(){
        return player2Turn;
    }

    public void switchTurn(){
        player2Turn = !player2Turn;
    }

    public char getTurnToken(){
        if (player2Turn)
            return player2;
        return player1;
    }

    public boolean gameOver(){
        return ticTacToe.isGameOver();
    }

    public TicTacToe getBoard(){
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
}
