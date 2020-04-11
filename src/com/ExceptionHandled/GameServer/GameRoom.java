package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Game.MoveMade;
import com.ExceptionHandled.GameMessages.Wrappers.Game;
import com.ExceptionHandled.GameMessages.Wrappers.Packet;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GameRoom implements Runnable {
    private String gameID;
    private String roomPassword;

    private String p1;
    private String p2;

    private BlockingQueue<ServerPacket> serverPacketQ;
    private Thread thread;

    public GameRoom(String gameID, String roomPassword, String p1) {
        this.gameID = gameID;
        this.roomPassword = roomPassword;
        this.p1 = p1;

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

    @Override
    public void run() {
        while(true){
            try {
                ServerPacket serverPacket = serverPacketQ.take();
                Packet packet = serverPacket.getPacket();

                //handle moves
                if(packet.getMessage() instanceof Game){
                    Game game = (Game)packet.getMessage();

                    if(game.getMessage() instanceof MoveMade){
                        MoveMade move = (MoveMade)game.getMessage();
                        //TODO: Send move to tictactoe game;
                    }
                }




            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
