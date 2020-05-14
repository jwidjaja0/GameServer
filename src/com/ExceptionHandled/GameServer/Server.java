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
import com.ExceptionHandled.GameServer.InternalMessage.ActivePlayerList;
import com.ExceptionHandled.GameServer.InternalMessage.ServerPacket;
import com.ExceptionHandled.GameServer.InternalMessage.UserInvolvement;
import com.ExceptionHandled.GameServer.Observer.GameLogicObserver;
import com.ExceptionHandled.GameServer.Observer.GameLogicSubject;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Server implements Runnable, GameLogicSubject {
    private BlockingQueue<ServerPacket> messageQueue;

    private List<ClientConnection> clientConnectionList;
    private List<GameRoom> gameRoomList;
    private Map<String, ClientConnection> activePlayerMapCC;
    private ListenNewClient listenNewClient;

    private Thread thread;

    private List<GameLogicObserver> observerList;

    private final String aiID = "AI";


    public Server() {
        messageQueue = new ArrayBlockingQueue<>(500);
        clientConnectionList = new ArrayList<>(100);
        gameRoomList = new ArrayList<>(100);
        listenNewClient = new ListenNewClient(clientConnectionList, messageQueue);
        activePlayerMapCC = new HashMap<>();

        //gameRoomList.add(new GameRoom("sampleID", "", "sample", "x"));
        observerList = new ArrayList<>();

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
            response = SQLiteQuery.getInstance().getGameHistoryDetailForPlayer(packet, request.getGameId());
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

    private void handleMainMenuMessage(ServerPacket serverPacket) throws IOException, InterruptedException {
        Packet packet = serverPacket.getPacket();
        Packet response = null;
        String playerID = packet.getPlayerID();

        if(packet.getMessage() instanceof NewGameRequest){
            NewGameRequest newGameRequest = (NewGameRequest)packet.getMessage();
            response = SQLiteQuery.getInstance().insertNewGame(packet);

            if(response.getMessage() instanceof NewGameSuccess){
                NewGameSuccess ngs = (NewGameSuccess)response.getMessage();
                String gameID = ngs.getGameId();
                String gameName = newGameRequest.getGameName();

                GameRoom gm = new GameRoom(gameID, gameName, playerID);
                System.out.println("New Game added");
                gameRoomList.add(gm);

                notifyGameLogicObserver(getListActiveGames());

                if (newGameRequest.getOpponent().equalsIgnoreCase("AI")) {
                    JoinGameRequest aiJoin = new JoinGameRequest(gameID);
                    Packet sPacket = new Packet("MainMenu", aiID, aiJoin);
                    messageQueue.put(new ServerPacket(serverPacket.getClientConnection(), sPacket));
                }
            }
        }

        else if(packet.getMessage() instanceof JoinGameRequest){
            JoinGameRequest request = (JoinGameRequest)packet.getMessage();
            String idRequest = request.getGameId();
            System.out.println("received joinGameRequest");

            for(GameRoom gm : gameRoomList){
                if(gm.getGameID().equals(idRequest)){
                    if (gm.getPlayer2() == null && !gm.getPlayer1().equalsIgnoreCase(playerID)) {
                        ArrayList<Packet> packets = gm.setPlayer2(playerID);
                        for (Packet notice : packets) {
                            System.out.println("sending to player");
                            if (!notice.getPlayerID().equals(aiID)) //if not to Ai
                                activePlayerMapCC.get(notice.getPlayerID()).getObjectOutputStream().writeObject(notice);
                        }
                        response = SQLiteQuery.getInstance().joinGame(packet);

                    }
                    else {
                        Packet notice = new Packet ("MainMenu", playerID, new JoinGameFail(idRequest));
                        serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);
                    }
                }
            }
            return;
        }

        else if(packet.getMessage() instanceof ListActiveGamesRequest){
            ListActiveGames listAG = getListActiveGames();
            response = new Packet("MainMenu", packet.getPlayerID(), listAG);
        }

        else if(packet.getMessage() instanceof SpectateRequest){
            SpectateRequest sr = (SpectateRequest)packet.getMessage();
            String gameID = sr.getGameId();

            for(GameRoom gm : gameRoomList){
                if(gm.getGameID().equals(gameID)){
                    SQLiteQuery.getInstance().insertViewerToGame(packet);
                    response = gm.addViewer(packet.getPlayerID());
                }
                else {
                    response = new Packet("MainMenu", playerID, new SpectateFail("Unknown spectate fail error"));
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
        GameRoom gr = null;
        boolean removeGame = false;

            //find the correct gameID
            for (GameRoom gm : gameRoomList) {

                if (gm.getGameID().equals(gameID)) {
                    ArrayList<Packet> packets = new ArrayList<Packet>(); //to send back

                    if (gameMessage instanceof MoveMade) {
                        System.out.println("MoveMade, setting packet back");
                        packets.addAll(gm.makeMove((MoveMade) gameMessage, packet.getPlayerID()));

                        //to remove game later from gameList if game over
                        for(Packet p: packets){
                            if(p.getMessage() instanceof GameOverOutcome){
                                removeGame = true;
                                gr = gm;
                            }
                        }
                    }

                    //send all packets
                    for (Packet notice : packets) {
                        if (!notice.getPlayerID().equals(aiID)) {
                            ClientConnection con = activePlayerMapCC.get(notice.getPlayerID());
                            ObjectOutputStream os = con.getObjectOutputStream();
                            os.writeObject(notice);
                            //originally: activePlayerMapCC.get(notice.getPlayerID()).getObjectOutputStream().writeObject(notice);
                        }
                        else {
                            System.out.println("dont send to AI " + packet.getMessageType());
                        }
                    }
                }
            }

        //removes game later from gameList if game over
        if (removeGame) {
            gameRoomList.remove(gr);
            notifyGameLogicObserver(getListActiveGames());
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

                notifyGameLogicObserver(getActivePlayers());
            }
        }

        else if(packet.getMessage() instanceof LogoutRequest){
            String playerID = packet.getPlayerID();
            if(playerID.equals(null)){
                response = new Packet("Login", playerID, new LogoutFail(""));
            }
            else{
                activePlayerMapCC.remove(playerID);
                response = new Packet("Login", playerID, new LogoutSuccess());
                notifyGameLogicObserver(getActivePlayers());
            }
        }

        //TODO: delete later, only for debugging
        if(response.getMessage() instanceof LoginSuccess){

            System.out.println(response.getPlayerID());
            System.out.println(response.getPlayerID());
        }
        serverPacket.getClientConnection().getObjectOutputStream().writeObject(response);
    }

    private ListActiveGames getListActiveGames(){
        List<ActiveGameHeader> gameList = new ArrayList<>();
        for(int i = 0; i < gameRoomList.size(); i++){
            gameList.add(gameRoomList.get(i).getActiveGameHeader());
        }
        System.out.println("Sending list active games, size: " + gameList.size());
        ListActiveGames listAG = new ListActiveGames(gameList);
        return listAG;
    }

    private ActivePlayerList getActivePlayers(){
        List<String> idList = new ArrayList<>();
        for(String key : activePlayerMapCC.keySet()){
            idList.add(key);
        }
        return new ActivePlayerList(idList);
    }

    public UserInvolvement findUserInvolvement(String playerID){
        List<String> listGamesPlaying = new ArrayList<>();
        List<String> listGamesViewing = new ArrayList<>();

        for(GameRoom gm : gameRoomList){
            if(gm.isPlayerIDInGame(playerID)){
                listGamesPlaying.add(gm.getGameName());
            }
            else if(gm.isPlayerIDViewer(playerID)){
                listGamesViewing.add(gm.getGameName());
            }
        }
        UserInvolvement userInvolvement = new UserInvolvement(listGamesPlaying, listGamesViewing);
        return userInvolvement;
    }

    @Override
    public void addGameLogicObserver(GameLogicObserver obs) {
        observerList.add(obs);
    }

    @Override
    public void removeGameLogicObserver(GameLogicObserver obs) {
        observerList.remove(obs);
    }

    @Override
    public void notifyGameLogicObserver(Object arg) {
        for(GameLogicObserver g: observerList){
            g.update(this, arg);
        }
    }
}


