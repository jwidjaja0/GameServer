package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Game.*;
import com.ExceptionHandled.GameMessages.MainMenu.*;
import com.ExceptionHandled.GameMessages.Wrappers.Packet;
import com.ExceptionHandled.GameServer.Database.SQLiteQuery;
import com.ExceptionHandled.GameServer.Game.TTTGame;


import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GameRoom {
    private String gameID;
    private String roomPassword;
    private String gameName;

    private ArrayList<String> viewers;
    private String player1;
    private String player2;

    private TTTGame game;
    private ArrayList<MoveValid> moves;

    public GameRoom(String gameID, String roomPassword, String gameName, String player1) {
        this.gameID = gameID;
        this.roomPassword = roomPassword;
        this.gameName = gameName;

        game = new TTTGame();
        moves = new ArrayList<MoveValid>();

        viewers = new ArrayList<String>();
        this.player1 = player1;
    }

    public String getGameID() {
        return gameID;
    }

    public String getRoomPassword() {
        return roomPassword;
    }

    protected String getPlayer1() {
        return player1;
    }

    protected String getPlayer2() {
        return player2;
    }

    public ArrayList<Packet> setPlayer2(String player2) {
        this.player2 = player2;

        PlayerJoined joined = new PlayerJoined(gameID, SQLiteQuery.getInstance().getUsername(player2), gameName);
        JoinGameSuccess join = new JoinGameSuccess(gameID, SQLiteQuery.getInstance().getUsername(player1), gameName, moves);

        ArrayList<Packet> packets = new ArrayList<Packet>();
        packets.add(new Packet ("Game", player1, joined));
        packets.add(new Packet("MainMenu", player2, join));
        packets.add(new Packet("Game", player1, new WhoseTurn(gameID, "x")));
        packets.add(new Packet("Game", player2, new WhoseTurn(gameID, "x")));
        return packets;
    }

    public Packet addViewer (String viewer) {
        viewers.add(viewer);
        String p1Name = SQLiteQuery.getInstance().getUsername(player1);
        String p2Name = SQLiteQuery.getInstance().getUsername(player2);
        SpectateSuccess join = new SpectateSuccess (gameID, gameName, p1Name, p2Name, moves);
        return new Packet("MainMenu", viewer, join);
    }

    public void removeViewer (String viewer)  {
        viewers.remove(viewer);
    }

    public ActiveGameHeader getActiveGameHeader(){
        return new ActiveGameHeader(gameID, gameName, player1, player2);
    }

    //TODO: fix this method to take input
    public ArrayList<Packet> gameForfeit() {
        game.switchTurn();
        ArrayList<Packet> packets = gameOver(game.getTurnToken());
        game.switchTurn();
        return packets;
    }

    private ArrayList<Packet> gameOver(String whoWon) {
        int winner;
        if (game.getWhoseTurn()) winner = 2;
        else winner = 1;
        SQLiteQuery.getInstance().updateGameOver(gameID, winner);


        ArrayList<Packet> packets = new ArrayList<Packet>();
        GameOverOutcome gameOver = new GameOverOutcome(gameID, whoWon);

        packets.add(new Packet("Game", player1, gameOver));
        packets.add(new Packet("Game", player2, gameOver));
        for (String viewer : viewers) {
            packets.add(new Packet("Game", viewer, gameOver));
        }

        return packets;
    }

    private ArrayList<Packet> makeValidMove(MoveValid move) {
        ArrayList<Packet> packets = new ArrayList<Packet>();

        packets.add(new Packet("Game", player1, move));
        packets.add(new Packet("Game", player2, move));

        for (String viewer : viewers) {
            packets.add(new Packet("Game", viewer, move));
        }

        game.setMove(move.getxCoord(), move.getyCoord(), game.getTurnToken().charAt(0));

        if (game.gameOver()) {
            packets.addAll(gameOver(game.whoWon()));
        }

        else {
            game.switchTurn();
            WhoseTurn turn = new WhoseTurn(gameID, game.getTurnToken());

            packets.add(new Packet ("Game", player1, turn));
            packets.add(new Packet ("Game", player2, turn));
        }
        return packets;
    }

    public ArrayList<Packet> makeMove(MoveMade move) {
        ArrayList<Packet> packets = new ArrayList<Packet>();
        //if invalid move
        if (!game.validMove(move.getxCoord(), move.getyCoord())) {
            MoveInvalid moveInvalid = new MoveInvalid(gameID, game.getTurnToken(), move.getxCoord(), move.getyCoord());
            packets.add(new Packet("Game", move.getPlayer(), moveInvalid));
        }
        //otherwise make the move
        else {
            MoveValid moveValid = new MoveValid(gameID, game.getTurnToken(), move.getxCoord(), move.getyCoord());
            SQLiteQuery.getInstance().insertMoveHistory(moveValid);
            packets.addAll(makeValidMove(moveValid));
        }

        return packets;
    }
}
