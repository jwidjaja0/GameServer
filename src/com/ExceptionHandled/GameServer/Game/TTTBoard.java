package com.ExceptionHandled.GameServer.Game;

public class TTTBoard {
    private char[][] board;//represents the bame board

    /**
     * TicTacToe()
     * Constructor: Creates a 3x3 2 dimensional array filled with ' '.
     */

    //makes an empty board
    public TTTBoard(){
        board = new char[3][3];
        fillBoard();
    }

    /**
     *
     * @param game
     */
    //creates an identical board
    public TTTBoard(TTTBoard game){
        this.board = game.deepCopyBoard();
    }

    //game is over if either player won or the board is full
    public boolean isGameOver(){
        return isWon('x') || isWon('o') || isFull();
    }

    //places the player token in a specific spot on the board
    public void setMove(int row, int col, char token){
        board[row][col] = token;
    }

    //checks if the player token has a winning set of moves
    public boolean isWon(char token) {
        //check horizontals
        for(int i = 0; i < 3; i++){
            if(board[i][0] == token
                    && board[i][1] == token
                    && board[i][2] == token) {
                return true;
            }
        }
        //check verticals
        for(int i = 0; i < 3; i++){
            if(board[0][i] == token
                    && board[1][i] == token
                    && board[2][i] == token) {
                return true;
            }
        }
        //check one diagonal
        if(board[0][0] == token
                && board[1][1] == token
                && board[2][2] == token){
            return true;
        }
        //check other diagonal
        if(board[0][2] == token
                && board[1][1] == token
                && board[2][0] == token){
            return true;
        }
        //otherwise they didn't win
        return false;
    }

    //returns true if neither player won but the board is full
    public boolean isDraw(){
        if (isFull() && !isWon('x') && !isWon('o'))
            return true;
        else
            return false;
    }

    //returns true of there are no available moves left
    public boolean isFull(){
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                if(board[i][j] == ' ')
                    return false;
            }
        }
        return true;
    }

    //fills the board with empty spots
    private void fillBoard(){
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board[i].length; j++){
                board[i][j] = ' ';
            }
        }
    }

    //returns a duplicate board
    private char[][] deepCopyBoard(){
        char[][] newBoard = new char[3][3];
        for (int i = 0; i < 3; ++i){
            for (int j = 0; j < 3; ++j){
                newBoard[i][j] = board[i][j];
            }
        }
        return newBoard;
    }

    //returns the size of the board
    public int getSideDim() {
        return board.length;
    }

    //returns the character in the x,y location
    public char getCharAt(int x, int y){
        return board[x][y];
    }

    //calculates how many moves are left in the game
    public int remainingMoves(){
        int count = 9;
        for (int i = 0; i < board.length; ++i){
            for (int j = 0; j < board[i].length; ++j){
                if (board[i][j] != ' '){
                    --count;
                }
            }
        }
        return count;
    }

    //resets the move at x,y to a blank
    protected void unSetMove(int row, int col){
        board[row][col] = ' ';
    }
}

