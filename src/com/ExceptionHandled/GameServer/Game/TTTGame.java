package com.ExceptionHandled.GameServer.Game;


public class TTTGame {
    private TTTBoard board;
    private boolean player2Turn;
    private String firstTurn;

    final String player1 = "x";
    final String player2 = "o";

    //create new board, let player1 start, add observer
    public TTTGame() {
        board = new TTTBoard();
        player2Turn = false;
        firstTurn = player1;
    }

    //returns the winner if someone won, "D" for draw, or "-" if the game isn't over yet
    public String whoWon(){
        if (board.isWon('x'))
            return "x";
        else if (board.isWon('o'))
            return "o";
        else if (board.isFull())
                return "d";
        return "-";
    }

    public boolean validMove (int row, int col) {
        return board.getCharAt(row, col) == ' ';
    }

    public void setMove(int row, int col, char token){
        board.setMove(row, col, token);
    }

    public boolean isPlayer2Turn(){
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

    public boolean isGameOver(){
        return board.isGameOver();
    }

    public TTTBoard getBoard(){
        return board;
    }

    public void reset(){
        String first = firstTurn;
        board = new TTTBoard();
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
