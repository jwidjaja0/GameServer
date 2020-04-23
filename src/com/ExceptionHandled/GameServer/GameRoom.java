package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Game.MoveMade;
import com.ExceptionHandled.GameMessages.Interfaces.Game;
import com.ExceptionHandled.GameMessages.MainMenu.ActiveGameHeader;
import com.ExceptionHandled.GameMessages.Wrappers.Packet;
import com.ExceptionHandled.GameServer.Game.Game;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GameRoom implements Runnable {
    private String gameID;
    private String roomPassword;
    private String gameName;

    private String p1;
    private String p2;
    private ArrayList<String> viewers;

    private Game game;

    private BlockingQueue<ServerPacket> serverPacketQ;
    private Thread thread;

    public GameRoom(String gameID, String roomPassword, String gameName, String p1) {
        this.gameID = gameID;
        this.roomPassword = roomPassword;
        this.gameName = gameName;
        this.p1 = p1;

        game = new Game();

        viewers = new ArrayList<String>();

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

    public void addViewer (String viewer) {
        viewers.add(viewer);
        EnterGame board = new EnterGame (gameID, game.getBoard());
        Packet notice = new Packet("EnterGame", viewer, board);
        serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);
    }

    public ActiveGameHeader getActiveGameHeader(){
        return new ActiveGameHeader(gameID, gameName, p1, p2);
    }

    @Override
    public void run() {
        while(true){
            try {
                ServerPacket packet = serverPacketQ.take();
                Packet notice = null;

                //handle moves
                if(packet.getMessage() instanceof Game){

                    if(packet.getMessage() instanceof MoveMade){

                        MoveMade move = (MoveMade)packet.getMessage();

                        //if invalid move
                        if (!game.validMove(move.getxCoord(), move.getyCoord())) { {
                                MoveInvalid moveInvalid = new MoveInvalid(gameID, message.getPlayer(), message.getxCoord(), message.getyCoord());
                                notice = new Packet("MoveInvalid", message.getPlayer(), moveInvalid);
                                serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);
                        }                        }
                        else {
                            MoveValid moveValid = new MoveValid(gameID, move.getPlayer(), move.getxCoord(), move.getyCoord();

                            notice = new Packet("MoveValid", p1, moveValid);
                            serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);

                            notice = new Packet("MoveValid", p2, moveValid);
                            serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);

                            for (String viewer : viewers) {
                                notice = new Packet("MoveValid", viewer, moveValid);
                                serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);
                            }

                            game.setMove(move.getxCoord(), move.getyCoord(), game.getTurnToken());

                            if (game.gameOver()) {
                                char win = game.whoWon();
                                GameOver gameOver;
                                if (win == 'X') {
                                    notice = new Packet("GameOverWin", gameID, p1);
                                    serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);

                                    notice = new Packet("GameOverLoss", gameID, p2);
                                    serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);

                                    gameOver = new GameOver(gameID, p1);
                                }
                                else if (win == 'O') {
                                    notice = new Packet("GameOverWin", gameID, p2);
                                    serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);

                                    notice = new Packet("GameOverLoss", gameID, p1);
                                    serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);

                                    gameOver = new GameOver(gameID, p2);
                                }
                                else {
                                    notice = new Packet("GameOverTie", gameID, p1);
                                    serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);

                                    notice = new Packet("GameOverTie", gameID, p2);
                                    serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);

                                    gameOver = new GameOver(gameID, "tie");
                                }

                                for (String viewer : viewers) {
                                    notice = new Packet("GameOver", viewer, gameOver);
                                    serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);
                                }
                            }

                            else {
                                game.switchTurn();
                                WhoseTurn turn = new WhoseTurn(gameID, game.getWhoseTurn());

                                notice = new Package("WhoseTurn", p1, turn);
                                serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);

                                notice = new Package("WhoseTurn", p2, turn);
                                serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);
                            }
                        }
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
