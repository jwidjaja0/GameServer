package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Connection.ConnectionRequest;
import com.ExceptionHandled.GameMessages.Login.SignUpFail;
import com.ExceptionHandled.GameMessages.Login.SignUpRequest;
import com.ExceptionHandled.GameMessages.Login.SignUpSuccess;
import com.ExceptionHandled.GameMessages.Wrappers.Packet;


import java.io.IOException;
import java.sql.*;
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

    private Connection connection;

    private Thread thread;

    public Server() {
        numRooms = 0;
        messageQueue = new ArrayBlockingQueue<>(500);
        clientConnectionList = new ArrayList<>(100);
        gameRoomList = new ArrayList<>(100);
        listenNewClient = new ListenNewClient(clientConnectionList, messageQueue);

        GameRoom gm = new GameRoom();
        gameRoomList.add(gm);



        thread = new Thread(this);
        thread.start();
        System.out.println("server instantiated");
    }

    @Override
    public void run() {
        System.out.println("Server thread started");

        while (true){
            try {
                connection = setConnection();
                ServerPacket serverPacket = messageQueue.take();
                Packet packet = serverPacket.getPacket();

                if(packet.getMessage() instanceof ConnectionRequest){
                    handleConnectionRequest(serverPacket);
                }
                else if(packet.getMessage() instanceof SignUpRequest){
                    handleSignupRequest(serverPacket);
                }



            } catch (InterruptedException | IOException | SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public void handleConnectionRequest(ServerPacket serverPacket){
        System.out.println("Connection request from client");
    }

    public void handleSignupRequest(ServerPacket serverPacket) throws IOException {
        Packet packet = serverPacket.getPacket();
        SignUpRequest request = (SignUpRequest) packet.getMessage();
        String usernameRequest = request.getUsername();
        String passwordRequest = request.getPassword();
        String id = serverPacket.getClientConnection().getId().toString();

        String query2 = "INSERT INTO 4blogin.playerinfo values(" + id + "," + usernameRequest+"," + passwordRequest + ")";
        Statement myStatement = null;
        try {
            myStatement = connection.createStatement();
            myStatement.executeUpdate(query2);

            //if insert successfully, return signupsuccess
            serverPacket.getClientConnection().getObjectOutputStream().writeObject(new SignUpSuccess());
        }
        catch(SQLIntegrityConstraintViolationException e){
            System.out.println("Duplicate username!");
            serverPacket.getClientConnection().getObjectOutputStream().writeObject(new SignUpFail("duplicate username"));
        }

        catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public Connection setConnection() throws SQLException {
        String URL = "jdbc:mysql://127.0.0.1:3306/";
        String user = "guest";
        String pw = "mypassword";

        String query = "select * from 4blogin.playerinfo;";
        Connection myConn = DriverManager.getConnection(URL, user, pw);
        return myConn;
    }
}
