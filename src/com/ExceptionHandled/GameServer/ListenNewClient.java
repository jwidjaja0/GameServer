package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Game.MoveMade;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ListenNewClient implements Runnable {
    private List<ClientConnection> clientConnectionList;
    private BlockingQueue<ServerPacket> messageQueue;
    private int clientNo;

    private Thread thread;

    public ListenNewClient(List<ClientConnection> clientConnectionList, BlockingQueue<ServerPacket> messageQueue) {
        this.clientConnectionList = clientConnectionList;
        this.messageQueue = messageQueue;
        clientNo = 0;

        thread = new Thread(this);
        thread.start();
    }


    @Override
    public void run() {
        try {
            System.out.println("ListenNewClient thread started");
            ServerSocket serverSocket = new ServerSocket(8000);

            while(true){
                Socket socket = serverSocket.accept();
                clientNo++;
                String startingMessage = "Starting thread for client " + clientNo + " at " + new Date() + '\n';

                System.out.println(startingMessage);
                InetAddress inetAddress = socket.getInetAddress();
                String clientInfoMessage = "Client " + clientNo + "'s host name is " + inetAddress.getHostName() + '\n'
                        + "Client " + clientNo + "'s host address is " + inetAddress.getHostAddress() + '\n';
                System.out.println(clientInfoMessage);

                ClientConnection clientConnection = new ClientConnection(socket,clientNo, messageQueue, clientConnectionList);
                clientConnectionList.add(clientConnection);

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
