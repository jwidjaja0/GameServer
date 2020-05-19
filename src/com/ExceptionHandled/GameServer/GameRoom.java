package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Game.*;
import com.ExceptionHandled.GameMessages.MainMenu.*;
import com.ExceptionHandled.GameMessages.Wrappers.Packet;
import com.ExceptionHandled.GameServer.Database.SQLiteQuery;
import com.ExceptionHandled.GameServer.Game.TTTGame;


import java.util.*;

public class GameRoom {
    private String gameID;
    private String gameName;

    private ArrayList<String> viewers;
    private String player1; //ID of player1
    private String player2; //ID of player2

    private TTTGame game;
    private ArrayList<MoveValid> moves;

    public boolean isPlayerIDInGame(String playerID){
        if(playerID.equals(player1) || playerID.equals(player2)){
            return true;
        }
        return false;
    }

    public boolean isPlayerIDViewer(String playerID){
        return viewers.contains(playerID);
    }

    public String getGameName() {
        return gameName;
    }

    public GameRoom(String gameID, String gameName, String player1) {
        this.gameID = gameID;
        this.gameName = gameName;

        game = new TTTGame();
        moves = new ArrayList<MoveValid>();

        viewers = new ArrayList<String>();
        this.player1 = player1;
    }

    public String getGameID() {
        return gameID;
    }

    protected String getPlayer1() {
        return player1;
    }

    protected String getPlayer2() {
        return player2;
    }

    public ArrayList<Packet> setPlayer2(String player2) {
        this.player2 = player2;
        ArrayList<Packet> packets = new ArrayList<Packet>();


        if (!player2.equals("AI")){//Dont send this message if player is playing vsAI, breaks client
            PlayerJoined joined = new PlayerJoined(gameID, SQLiteQuery.getInstance().getUsername(player2), gameName);
            packets.add(new Packet ("Game", player1, joined));
        }

        JoinGameSuccess join = new JoinGameSuccess(gameID, SQLiteQuery.getInstance().getUsername(player1), gameName, moves);
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

    private ArrayList<Packet> gameOver(String whoWon) {
        int gameStatus;
        if(whoWon.equals("x")){
            gameStatus = 1;
        } else if(whoWon.equals("o")){
            gameStatus = 2;
        } else{
            gameStatus = 3;
        }

        SQLiteQuery.getInstance().updateGameOver(gameID, gameStatus, player1, player2);

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

        game.setMove(move.getXCoord(), move.getYCoord(), game.getTurnToken().charAt(0));

        if (game.isGameOver()) {
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

    public ArrayList<Packet> makeMove(MoveMade move, String playerID) {
        ArrayList<Packet> packets = new ArrayList<Packet>();
        //if invalid move
        if (!game.validMove(move.getxCoord(), move.getyCoord())) {
            MoveInvalid moveInvalid = new MoveInvalid(gameID, game.getTurnToken(), move.getxCoord(), move.getyCoord());
            packets.add(new Packet("Game", playerID, moveInvalid));
        }
        //otherwise make the move
        else {
            MoveValid moveValid = new MoveValid(gameID, game.getTurnToken(), move.getxCoord(), move.getyCoord());
            SQLiteQuery.getInstance().insertMoveHistory(moveValid);
            packets.addAll(makeValidMove(moveValid));
            moves.add(moveValid);
        }
        return packets;
    }
}
