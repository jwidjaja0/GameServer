package com.ExceptionHandled.GameServer.Game;

import java.io.IOException;
import com.ExceptionHandled.GameMessages.Game.MoveMade;
import com.ExceptionHandled.GameServer.Player;

public class AIPlayer extends Player {//have implement observable
    private TicTacToe board;

    public AIPlayer() throws IOException {
        super();
    }

    //returns the best move the AI could make to win
    public MoveMade makeMove(){
        int bestMove = 100; //will be the fewest number of moves to win, or the largest number of moves to loose/draw
        int bestMoveX = -10; //board position of best move
        int bestMoveY = -5; //board position of best move
        TicTacToe tempBoard = new TicTacToe(board); //copy of board to be used

        ///iterate through all possible moves...
        for (int x = 0; x < tempBoard.getSideDim(); x++){
            for (int y = 0; y < tempBoard.getSideDim(); y++){
                if (tempBoard.getCharAt(x, y) == ' '){
                    //test the benefits of this move
                    tempBoard.setMove(x, y, 'O');
                    int temp = minimax(tempBoard, board.remainingMoves()-1, false, -100, 100);
                    //if its the best move so far, then save it
                    if (temp < bestMove){
                        bestMove = temp;
                        bestMoveX = x;
                        bestMoveY = y;
                    }
                    //then delete the move to test the other moves
                    tempBoard.unSetMove(x, y);
                }
            }
        }
        //return the best move
        return new MoveMade(bestMoveX, bestMoveY);
    }

    public int minimax (TicTacToe board, int depth, boolean maxPlayer, int alpha, int beta) {
        if (board.isWon('X')) return 10 + board.remainingMoves(); //X is max player
        if (board.isWon('O')) return -10 - board.remainingMoves(); //O is min player
        if (depth == 0) return 0; //tie)


        int eval;

        if (!maxPlayer) {
            for (int x = 0; x < board.getSideDim(); x++) {
                for (int y = 0; y < board.getSideDim(); y++) {
                    if (board.getCharAt(x,y) == ' ') {
                        board.setMove(x,y,'X');
                        eval = minimax(board, depth - 1, !maxPlayer, alpha, beta);
                        board.unSetMove(x,y);
                        if (eval > alpha) alpha = eval;
                    }
                }
            }
            return alpha;
        }

        else {
            for (int x = 0; x < board.getSideDim(); x++) {
                for (int y = 0; y < board.getSideDim(); y++) {
                    if (board.getCharAt(x,y) == ' ') {
                        board.setMove(x,y,'O');
                        eval = minimax(board, depth - 1, !maxPlayer, alpha, beta);
                        board.unSetMove(x,y);
                        if (eval < beta) beta = eval;
                    }
                }
            }
            return beta;
        }
    }

    public void updateBoard(TicTacToe board){
        this.board = board;
    }
}
