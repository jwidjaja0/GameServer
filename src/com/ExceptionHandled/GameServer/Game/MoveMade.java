package com.ExceptionHandled.GameServer.Game;

public class MoveMade {
    private int row;
    private int col;

    private MoveMade(){
    }

    public MoveMade(int row, int col){
        this.row = row;
        this.col = col;
    }

    public int getxCoord() {
        return row;
    }

    public int getyCoord() {
        return col;
    }

    @Override
    public String toString() {
        return "( " + row + ", " + col + " )";
    }
}
