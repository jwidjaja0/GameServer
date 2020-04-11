package com.ExceptionHandled.GameServer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GameRoom implements Runnable {
    private String gameID;

    private String p1;
    private String p2;

    private BlockingQueue<ServerPacket> serverPacketQ;
    private Thread thread;

    public GameRoom(String gameID, String p1) {
        this.gameID = gameID;
        this.p1 = p1;

        serverPacketQ = new ArrayBlockingQueue<>(20);
    }

    public String getGameID() {
        return gameID;
    }

    public void addToMessageQ(ServerPacket sp){
        serverPacketQ.add(sp);
    }

    @Override
    public void run() {

    }
}
