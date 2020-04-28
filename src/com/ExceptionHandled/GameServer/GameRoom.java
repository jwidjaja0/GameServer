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

public class GameRoom implements Runnable {
    private String gameID;
    private String roomPassword;
    private String gameName;

    private Map<String, ClientConnection> players;
    private Map<String, ClientConnection> viewers;
    private String player1;
    private String player2;

    private TTTGame game;
    private ArrayList<MoveValid> moves;

    private BlockingQueue<ServerPacket> serverPacketQ;
    private Thread thread;

    public GameRoom(String gameID, String roomPassword, String gameName, String player1, ClientConnection connection1) {
        this.gameID = gameID;
        this.roomPassword = roomPassword;
        this.gameName = gameName;

        game = new TTTGame();
        moves = new ArrayList<MoveValid>();

        players = new HashMap<>();
        players.put(player1, connection1);
        viewers = new HashMap<>();
        this.player1 = player1;

        serverPacketQ = new ArrayBlockingQueue<>(20);
        thread = new Thread(this);
        thread.start();
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

    public void setPlayer2(String player2, ClientConnection connection2) {
        players.put(player2, connection2);
        this.player2 = player2;
    }

    public void addToMessageQ(ServerPacket sp){
        serverPacketQ.add(sp);
    }

    public void addViewer (String viewer, ClientConnection connection) throws IOException {
        viewers.put(viewer, connection);
        SpectateSuccess join = new SpectateSuccess (gameID, gameName, player1, player2, moves);
        Packet notice = new Packet("EnterGame", viewer, join);
        connection.getObjectOutputStream().writeObject(notice);
    }

    public void removeViewer (String viewer) throws IOException {
        SpectatorLeave leave = new SpectatorLeave(gameID);
        Packet notice = new Packet("EnterGame", viewer, leave);
        viewers.get(viewer).getObjectOutputStream().writeObject(notice);
        viewers.remove(viewer);
    }

    public ActiveGameHeader getActiveGameHeader(){
        return new ActiveGameHeader(gameID, gameName, player1, player2);
    }

    //TODO: fix this method to take input
    public void gameOver () throws IOException {
        game.switchTurn();
        gameOver(game.getTurnToken());
        game.switchTurn();
    }

    private void gameOver(String whoWon) throws IOException {
        Packet notice;

        GameOverOutcome gameOver = new GameOverOutcome(gameID, whoWon);

        for (String player : players.keySet()) {
            notice = new Packet("GameOverOutcome", player, gameOver);
            players.get(player).getObjectOutputStream().writeObject(notice);
        }

        for (String viewer : viewers.keySet()) {
            notice = new Packet("GameOverOutcome", viewer, gameOver);
            viewers.get(viewer).getObjectOutputStream().writeObject(notice);
        }
    }

    private void makeValidMove(MoveValid move) throws IOException {
        Packet notice;

        for (String player : players.keySet()) {
            notice = new Packet("MoveValid", player, move);
            players.get(player).getObjectOutputStream().writeObject(notice);
        }

        for (String viewer : viewers.keySet()) {
            notice = new Packet("MoveValid", viewer, move);
            viewers.get(viewer).getObjectOutputStream().writeObject(notice);
        }

        game.setMove(move.getxCoord(), move.getyCoord(), game.getTurnToken().charAt(0));

        if (game.gameOver()) {
            gameOver(game.whoWon());
        }

        else {
            game.switchTurn();
            WhoseTurn turn = new WhoseTurn(gameID, game.getTurnToken());

            for (String player : players.keySet()) {
                notice = new Packet ("WhoseTurn", player, turn);
                players.get(player).getObjectOutputStream().writeObject(notice);
            }
        }
    }

    public void makeMove(MoveMade move) throws IOException {
        //if invalid move
        if (!game.validMove(move.getxCoord(), move.getyCoord())) {
            MoveInvalid moveInvalid = new MoveInvalid(gameID, game.getTurnToken(), move.getxCoord(), move.getyCoord());
            Packet notice = new Packet("MoveInvalid", move.getPlayer(), moveInvalid);
            players.get(move.getPlayer()).getObjectOutputStream().writeObject(notice);
        }
        //otherwise make the move
        else {
            MoveValid moveValid = new MoveValid(gameID, game.getTurnToken(), move.getxCoord(), move.getyCoord());
            SQLiteQuery.getInstance().insertMoveHistory(moveValid);
            makeValidMove(moveValid);
        }
    }

    @Override
    public void run() {
        while(true){
            try {
                ServerPacket packet = serverPacketQ.take();
                ClientConnection connection = packet.getClientConnection();
                Packet notice = null;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
