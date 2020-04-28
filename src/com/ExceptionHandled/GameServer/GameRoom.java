package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Game.*;
import com.ExceptionHandled.GameMessages.MainMenu.*;
import com.ExceptionHandled.GameMessages.Wrappers.Packet;
import com.ExceptionHandled.GameServer.Database.SQLiteQuery;
import com.ExceptionHandled.GameServer.Game.TTTGame;


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

    public void setPlayer2(String player2, ClientConnection connection2) {
        players.put(player2, connection2);
        this.player2 = player2;
    }

    public void addToMessageQ(ServerPacket sp){
        serverPacketQ.add(sp);
    }

    public void addViewer (String viewer, ClientConnection connection) {
        viewers.put(viewer, connection);
        SpectateSuccess join = new SpectateSuccess (gameID, gameName, player1, player2, moves);
        Packet notice = new Packet("EnterGame", viewer, join);
        connection.getObjectOutputStream().writeObject(notice);
    }

    public void removeViewer (String viewer) {
        SpectatorLeave leave = new SpectatorLeave(gameID);
        Packet notice = new Packet("EnterGame", viewer, leave);
        viewers.get(viewer).getConnectionID().getObjectOutputStream().writeObject(notice);
        viewers.remove(viewer);
    }

    public ActiveGameHeader getActiveGameHeader(){
        return new ActiveGameHeader(gameID, gameName, player1, player2);
    }

    public void gameOver () {
        game.switchTurn();
        gameOver(game.getTurnToken());
        game.switchTurn();
    }

    private void gameOver(char whoWon) {
        Packet notice;

        GameOverOutcome gameOver = new GameOverOutcome(gameID, whoWon);

        notice = new Packet("GameOverOutcome", player1, gameOver);
        serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);

        notice = new Packet("GameOverOutcome", player2, gameOver);
        serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);

        for (String viewer : viewers) {
            notice = new Packet("GameOverOutcome", viewer, gameOver);
            serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);
        }
    }

    private void makeValidMove(MoveValid move) {
        Packet notice;

        notice = new Packet("MoveValid", player1, move);
        serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);

        notice = new Packet("MoveValid", player2, move);
        serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);

        for (String viewer : viewers) {
            notice = new Packet("MoveValid", viewer, move);
            serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);
        }

        game.setMove(move.getxCoord(), move.getyCoord(), game.getTurnToken());

        if (game.gameOver()) {
            gameOver(game.whoWon());
        }

        else {
            game.switchTurn();
            WhoseTurn turn = new WhoseTurn(gameID, game.getWhoseTurn());

            notice = new Package("WhoseTurn", player1, turn);
            serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);

            notice = new Package("WhoseTurn", player2, turn);
            serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);
        }
    }

    public void makeMove(MoveMade move) {
        //if invalid move
        if (!game.validMove(move.getxCoord(), move.getyCoord())) {
            MoveInvalid moveInvalid = new MoveInvalid(gameID, game.getTurnToken(), move.getxCoord(), move.getyCoord());
            Packet notice = new Packet("MoveInvalid", move.getPlayer(), moveInvalid);
            serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);
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
