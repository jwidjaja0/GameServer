package com.ExceptionHandled.GameServer;


import com.ExceptionHandled.GameMessages.Wrappers.Packet;
import com.ExceptionHandled.GameServer.InternalMessage.ServerPacket;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class Outgoing {
    private BlockingQueue<ServerPacket> outgoingQueue;
    private static Outgoing instance = new Outgoing();

    private Map<String, ClientConnection> activePlayerMapCC;

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

    public void addToQueue(Packet packet, String playerID){
        ClientConnection cc = activePlayerMapCC.get(playerID);
        addToQueue(packet, cc);
    }

    public void setActivePlayerMapCC(Map<String, ClientConnection> activePlayerMapCC) {
        this.activePlayerMapCC = activePlayerMapCC;
    }
}
