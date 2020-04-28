package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Game.MoveMade;
import com.ExceptionHandled.GameMessages.Interfaces.Game;
import com.ExceptionHandled.GameMessages.MainMenu.ActiveGameHeader;
import com.ExceptionHandled.GameMessages.Wrappers.Packet;
import com.ExceptionHandled.GameServer.Game.TicTacToe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GameRoom implements Runnable {
    private String gameID;
    private String roomPassword;
    private String gameName;

    private String p1;
    private String p2;

    private TicTacToe ttt;
    private List<String>spectatorListID;

    private BlockingQueue<ServerPacket> serverPacketQ;
    private Thread thread;

    public GameRoom(String gameID, String roomPassword, String gameName, String p1) {
        this.gameID = gameID;
        this.roomPassword = roomPassword;
        this.gameName = gameName;
        this.p1 = p1;

        spectatorListID = new ArrayList<>();

        ttt = new TicTacToe();

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

    public void setP2(String p2) {
        this.p2 = p2;
    }

    public void addToMessageQ(ServerPacket sp){
        serverPacketQ.add(sp);
    }

    public void addSpectator(String id){
        spectatorListID.add(id);
    }

    public List<String> getSpectatorListID(){
        return spectatorListID;
    }

    public ActiveGameHeader getActiveGameHeader(){
        return new ActiveGameHeader(gameID, gameName, p1, p2);
    }

    @Override
    public void run() {
        while(true){
            try {
                ServerPacket serverPacket = serverPacketQ.take();
                Packet packet = serverPacket.getPacket();

                //handle moves
                if(packet.getMessage() instanceof Game){

                    if(packet.getMessage() instanceof MoveMade){
                        MoveMade move = (MoveMade)packet.getMessage();
                        //TODO: Send move to tictactoe game;
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
