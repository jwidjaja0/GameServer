package com.ExceptionHandled.GameServer;


import com.ExceptionHandled.GameMessages.Wrappers.Packet;
import com.ExceptionHandled.GameServer.InternalMessage.ServerPacket;

import java.util.concurrent.BlockingQueue;

public class Outgoing {
    private BlockingQueue<ServerPacket> outgoingQueue;
    private static Outgoing instance = new Outgoing();

    private Outgoing() {
    }

    public static Outgoing getInstance(){
        return instance;
    }

    public void setOutgoingQueue(BlockingQueue<ServerPacket> outgoingQueue) {
        this.outgoingQueue = outgoingQueue;
    }

    public void addToQueue(ServerPacket serverPacket){
        try {
            outgoingQueue.put(serverPacket);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addToQueue(Packet packet, ClientConnection clientConnection){
        ServerPacket sp = new ServerPacket(clientConnection, packet);
        try {
            outgoingQueue.put(sp);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }






}
