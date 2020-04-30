package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Connection.*;
import com.ExceptionHandled.GameMessages.Game.*;
import com.ExceptionHandled.GameMessages.Interfaces.*;
import com.ExceptionHandled.GameMessages.Login.*;
import com.ExceptionHandled.GameMessages.MainMenu.*;
import com.ExceptionHandled.GameMessages.Stats.*;
import com.ExceptionHandled.GameMessages.UserUpdate.*;
import com.ExceptionHandled.GameMessages.Wrappers.Packet;
import com.ExceptionHandled.GameServer.Database.SQLiteQuery;


import java.io.IOException;
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

        //gameRoomList.add(new GameRoom("sampleID", "", "sample", "x"));

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
                else if(packet.getMessage() instanceof Login){
                    handleLoginMessages(serverPacket);
                }
                else if(packet.getMessage() instanceof MainMenu){
                    handleMainMenuMessage(serverPacket);
                }
                else if(packet.getMessage() instanceof Game){
                    handleGameMessage(serverPacket);
                }
                else if(packet.getMessage() instanceof UserUpdate){
                    handleUserUpdateMessage(serverPacket);
                }
                else if(packet.getMessage() instanceof Stats){
                    handleStatsMessage(serverPacket);
                }

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleStatsMessage(ServerPacket serverPacket) throws IOException {
        Packet packet = serverPacket.getPacket();
        System.out.println("handleStatsMessage, playerID: " + packet.getPlayerID());
        Packet response = null;

        if(packet.getMessage() instanceof GameHistoryRequest){
            GameHistoryRequest request = (GameHistoryRequest)packet.getMessage();
            response = SQLiteQuery.getInstance().getGameHistoryDetail(packet, request.getGameId());
        }
        else if(packet.getMessage() instanceof PlayerStatsRequest){
            PlayerStatsRequest playerStatsRequest = (PlayerStatsRequest)packet.getMessage();
            response = SQLiteQuery.getInstance().getPlayerStatsInfo(packet);
        }
        serverPacket.getClientConnection().getObjectOutputStream().writeObject(response);
    }

    private void handleUserUpdateMessage(ServerPacket serverPacket) throws IOException {
        Packet packet = serverPacket.getPacket();
        System.out.println("handleUserUpdateMessage, playerID: " + packet.getPlayerID());
        Packet response = null;

        if(packet.getMessage() instanceof UserUpdateRequest){
            response = SQLiteQuery.getInstance().updateUserInfo(packet);
        }
        else if(packet.getMessage() instanceof UserDeleteRequest){
            response = SQLiteQuery.getInstance().userDelete(packet);
        }

        serverPacket.getClientConnection().getObjectOutputStream().writeObject(response);
    }

    private void handleMainMenuMessage(ServerPacket serverPacket) throws IOException {
        Packet packet = serverPacket.getPacket();
        Packet response = null;

        if(packet.getMessage() instanceof NewGameRequest){
            NewGameRequest newGameRequest = (NewGameRequest)packet.getMessage();
            response = SQLiteQuery.getInstance().insertNewGame(packet);

            if(response.getMessage() instanceof NewGameSuccess){
                NewGameSuccess ngs = (NewGameSuccess)response.getMessage();
                String gameID = ngs.getGameId();
                String pw = newGameRequest.getGamePassword();
                String gameName = newGameRequest.getGameName();

                String playerID = packet.getPlayerID();
                GameRoom gm = new GameRoom(gameID, pw, gameName, playerID);
                System.out.println("New Game added");
                gameRoomList.add(gm);
            }
        }

        else if(packet.getMessage() instanceof JoinGameRequest){
            JoinGameRequest request = (JoinGameRequest)packet.getMessage();
            String idRequest = request.getGameId();
            String pw = request.getGamePassword();

            for(GameRoom g : gameRoomList){
                if(g.getGameID().equals(idRequest) && g.getRoomPassword().equals(pw)){
                    if (g.getPlayer2() != null) {
                        ArrayList<Packet> packets = g.setPlayer2(request.getRequestingPlayerId());
                        for (Packet notice : packets) {
                            activePlayerMapCC.get(notice.getPlayerID()).getObjectOutputStream().writeObject(notice);
                        }
                        response = SQLiteQuery.getInstance().joinGame(packet);
                    }
                    else {
                        Packet notice = new Packet ("JoinGameFail", request.getRequestingPlayerId(), new JoinGameFail(idRequest));
                        serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);
                    }
                }
            }
        }

        else if(packet.getMessage() instanceof ListActiveGamesRequest){
            List<ActiveGameHeader> gameList = new ArrayList<>();
            for(int i = 0; i < gameRoomList.size(); i++){
                gameList.add(gameRoomList.get(i).getActiveGameHeader());
            }
            System.out.println("Sending list active games, size: " + gameList.size());
            response = new Packet("MainMenu", packet.getPlayerID(), new ListActiveGames(gameList));
        }

        else if(packet.getMessage() instanceof SpectateRequest){
            SpectateRequest sr = (SpectateRequest)packet.getMessage();
            String gameID = sr.getGameId();

            for(GameRoom gm : gameRoomList){
                if(gm.getGameID().equals(gameID)){
                    Packet notice = gm.addViewer(packet.getPlayerID());
                    activePlayerMapCC.get(notice.getPlayerID()).getObjectOutputStream().writeObject(notice);
                    response = SQLiteQuery.getInstance().insertViewerToGame(packet);
                }
            }
        }

        else if(packet.getMessage() instanceof SpectatorLeave){
            SpectatorLeave sl = (SpectatorLeave)packet.getMessage();
            String gameID = sl.getGameID();

            for(GameRoom gm : gameRoomList){
                if(gm.getGameID().equals(gameID)){
                    gm.removeViewer(packet.getPlayerID());
                }
            }
        }

        serverPacket.getClientConnection().getObjectOutputStream().writeObject(response);
    }

    private void handleGameMessage(ServerPacket serverPacket) throws IOException {
        Packet packet = serverPacket.getPacket();
        Game gameMessage = (Game)packet.getMessage();
        String gameID = gameMessage.getGameID();

            //find the correct gameID
            for(GameRoom gm : gameRoomList) {
                if (gm.getGameID().equals(gameID)) {
                    ArrayList<Packet> packets = new ArrayList<Packet>();

                    if (gameMessage instanceof MoveMade) {
                        packets.addAll(gm.makeMove((MoveMade) gameMessage));
                    }

                    else if (gameMessage instanceof RematchRequest) {
                        RematchRequest message = (RematchRequest) gameMessage;
                        String pID = packet.getPlayerID();
                        if (pID.equals(gm.getPlayer1())) {
                            pID = gm.getPlayer2();
                        }
                        packets.add(new Packet("RematchRequest", message.getGameID(), pID));
                    }

                    else if (gameMessage instanceof RematchRespond) {
                        RematchRespond message = (RematchRespond) gameMessage;
                        String pID = packet.getPlayerID();
                        if (pID.equals(gm.getPlayer1())) {
                            pID = gm.getPlayer2();
                        }
                        packets.add(new Packet("RematchRespond", message.getGameID(), pID));
                    }
                    //TODO: currently can only forfeit on your turn, fix to forfeit whenever
                    else if (gameMessage instanceof ForfeitGame) {
                        packets.addAll(gm.gameForfeit());
                    }

                    //send all packets
                    for (Packet notice : packets) {
                        activePlayerMapCC.get(notice.getPlayerID()).getObjectOutputStream().writeObject(notice);
                    }
                }
            }
    }

    public void handleConnectionRequest(ServerPacket serverPacket){
        System.out.println("Connection request from client");
    }

    public void handleLoginMessages(ServerPacket serverPacket) throws IOException {
        Packet packet = serverPacket.getPacket();
        Packet response = null;

        if(packet.getMessage() instanceof SignUpRequest){
            response = SQLiteQuery.getInstance().insertNewUser(packet);
        }
        else if(packet.getMessage() instanceof LoginRequest){
            response = SQLiteQuery.getInstance().userLoggingIn(packet);
            System.out.println("LoginSuccess response");

            if(response.getMessage() instanceof LoginSuccess){
                LoginSuccess lg = (LoginSuccess) response.getMessage();
                activePlayerMapCC.put(lg.getPlayerID(), serverPacket.getClientConnection());
            }
        }

        else if(packet.getMessage() instanceof LogoutRequest){
            String playerID = packet.getPlayerID();
            if(playerID.equals(null)){
                //TODO: FIX LOGOUTFAIL
                response = new Packet("Login", playerID, new LogoutFail(""));
            }
            else{
                activePlayerMapCC.remove(playerID);
                response = new Packet("Login", playerID, new LogoutSuccess());
            }
        }

        //TODO: delete later, only for debugging
        if(response.getMessage() instanceof LoginSuccess){

            System.out.println(response.getPlayerID());
            System.out.println(response.getPlayerID());
        }
        serverPacket.getClientConnection().getObjectOutputStream().writeObject(response);
    }
}


