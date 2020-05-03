package com.ExceptionHandled.GameServer.Game;


public class TTTGame {
    private TicTacToe ticTacToe;
    private boolean player2Turn;
    private String firstTurn;

    final String player1 = "x";
    final String player2 = "o";

    //create new board, let player1 start, add observer
    public TTTGame() {
        ticTacToe = new TicTacToe();
        player2Turn = false;
        firstTurn = player1;
    }

    //returns the winner if someone won, "D" for draw, or "-" if the game isn't over yet
    public String whoWon(){
        if (ticTacToe.isWon('x'))
            return "x";
        else if (ticTacToe.isWon('o'))
            return "o";
        else if (ticTacToe.isFull())
                return "D";
        return "-";
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

    public String getTurnToken(){
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
        String first = firstTurn;
        ticTacToe = new TicTacToe();
        if (first.equals(player1)){
            player2Turn = true;
            firstTurn = player2;
        }
        else {
            player2Turn = false;
            firstTurn = player1;
        }
    }
}
