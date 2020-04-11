package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Connection.ConnectionRequest;
import com.ExceptionHandled.GameMessages.Game.MoveMade;
import com.ExceptionHandled.GameMessages.Login.*;
import com.ExceptionHandled.GameMessages.MainMenu.JoinGameRequest;
import com.ExceptionHandled.GameMessages.MainMenu.NewGameRequest;
import com.ExceptionHandled.GameMessages.MainMenu.NewGameSuccess;
import com.ExceptionHandled.GameMessages.Wrappers.Game;
import com.ExceptionHandled.GameMessages.Wrappers.Login;
import com.ExceptionHandled.GameMessages.Wrappers.MainMenu;
import com.ExceptionHandled.GameMessages.Wrappers.Packet;
import com.ExceptionHandled.GameServer.Database.SQLiteQuery;


import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Server implements Runnable {
    private BlockingQueue<ServerPacket> messageQueue;

    private List<ClientConnection> clientConnectionList;
    private List<GameRoom> gameRoomList;

    private Map<String, ClientConnection> activePlayerMapCC;

    private ListenNewClient listenNewClient;

    private Thread thread;

    public Server() {
        messageQueue = new ArrayBlockingQueue<>(500);
        clientConnectionList = new ArrayList<>(100);
        gameRoomList = new ArrayList<>(100);
        listenNewClient = new ListenNewClient(clientConnectionList, messageQueue);
        activePlayerMapCC = new HashMap<>();

        thread = new Thread(this);
        thread.start();
        System.out.println("server instantiated");
    }

    @Override
    public void run() {
        SQLiteQuery.getInstance().setConnection();
        System.out.println("Server thread started");

        while (true){
            try {
                ServerPacket serverPacket = messageQueue.take();
                Packet packet = serverPacket.getPacket();

                if(packet.getMessage() instanceof ConnectionRequest){
                    handleConnectionRequest(serverPacket);
                }
                else if(packet.getMessageType().equals("Login")){
                    handleLoginMessages(serverPacket);
                }
                else if(packet.getMessage() instanceof MainMenu){

                }

                else if(packet.getMessage() instanceof Game){
                    handleGameMessage(serverPacket);
                }

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void handleMainMenuMessage(ServerPacket serverPacket) throws IOException {
        Packet packet = serverPacket.getPacket();
        MainMenu menu = (MainMenu)packet.getMessage();

        MainMenu menuResponse = null;

        //start new game
        if(menu.getMessage() instanceof NewGameRequest){
            NewGameRequest newGameRequest = (NewGameRequest)menu.getMessage();

            menuResponse = SQLiteQuery.getInstance().insertNewGame(newGameRequest);

            if(menuResponse.getMessage() instanceof NewGameSuccess){
                NewGameSuccess ngs = (NewGameSuccess)menuResponse.getMessage();
                String gameID = ngs.getGameId();
                String pw = newGameRequest.getGamePassword();

                GameRoom gm = new GameRoom(gameID, pw, menuResponse.getPlayerID());
                gameRoomList.add(gm);
            }

            serverPacket.getClientConnection().getObjectOutputStream().writeObject(new Packet("MainMenu", menuResponse));
        }

        else if(menu.getMessage() instanceof JoinGameRequest){
            JoinGameRequest request = (JoinGameRequest)menu.getMessage();
            String idRequest = request.getGameId();
            String pw = request.getGamePassword();

            for(GameRoom g : gameRoomList){

                //TODO: Add check two players already set, can't set p2 if another request comes.
                if(g.getGameID().equals(idRequest) && g.getRoomPassword().equals(pw)){
                    g.setP2(request.getRequestingPlayerId());
                }
            }
        }
    }

    private void handleGameMessage(ServerPacket serverPacket) {
        Packet packet = serverPacket.getPacket();
        Game gameMessage = (Game)packet.getMessage();

        String gameID = gameMessage.getGameID(); //null for newgamerequest

        if(gameMessage.getMessage() instanceof MoveMade){
            //find the correct gameID
            for(GameRoom gm : gameRoomList){
                if(gm.getGameID().equals(gameID)){
                    gm.addToMessageQ(serverPacket);
                }
            }
        }
    }

    public void handleConnectionRequest(ServerPacket serverPacket){
        System.out.println("Connection request from client");
    }

    public void handleLoginMessages(ServerPacket serverPacket) throws IOException {
        Packet packet = serverPacket.getPacket();
        Login login = (Login)packet.getMessage();

        Login response = null;

        if(login.getMessage() instanceof SignUpRequest){
            SignUpRequest s = (SignUpRequest)login.getMessage();
            response = SQLiteQuery.getInstance().insertNewUser(s);

//            String connectionID = serverPacket.getClientConnection().getConnectionID();
        }
        else if(login.getMessage() instanceof LoginRequest){
            LoginRequest r = (LoginRequest)login.getMessage();

            response = SQLiteQuery.getInstance().userLoggingIn(r);
            if(response.getMessage() instanceof LoginSuccess){
                LoginSuccess lg = (LoginSuccess)response.getMessage();
                activePlayerMapCC.put(lg.getPlayerID(), serverPacket.getClientConnection());
            }
        }

        else if(login.getMessage() instanceof SignOutRequest){
            SignOutRequest signOutRequest = (SignOutRequest)login.getMessage();
            String playerID = signOutRequest.getPlayerID();
            activePlayerMapCC.remove(playerID);

            response = new Login("SignOutSuccess", new SignOutSuccess());
        }

        serverPacket.getClientConnection().getObjectOutputStream().writeObject(new Packet("Login", response));
    }
}
