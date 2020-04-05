package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Login.SignUpRequest;
import com.ExceptionHandled.GameMessages.MainMenu.ListActiveGames;
import com.ExceptionHandled.GameMessages.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Server implements Runnable {
    private int numRooms;

    private BlockingQueue<ServerPacket> messageQueue;

    private List<ClientConnection> clientConnectionList;
    private List<GameRoom> gameRoomList;

    private ListenNewClient listenNewClient;

    private Thread thread;

    public Server() {
        numRooms = 0;
        messageQueue = new ArrayBlockingQueue<>(500);
        clientConnectionList = new ArrayList<>(100);
        gameRoomList = new ArrayList<>(100);
        listenNewClient = new ListenNewClient(clientConnectionList, messageQueue);

        GameRoom gm = new GameRoom();
        gameRoomList.add(gm);

        thread = new Thread();
        thread.start();
    }

    @Override
    public void run() {
        while (true){
            try {
                ServerPacket serverPacket = messageQueue.take();
                Packet packet = serverPacket.getPacket();

                if(packet.getMessage() instanceof SignUpRequest)



            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
