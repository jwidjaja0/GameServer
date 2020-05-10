package com.ExceptionHandled.GameServer.Database;

import com.ExceptionHandled.GameMessages.Game.MoveValid;
import com.ExceptionHandled.GameMessages.Login.*;
import com.ExceptionHandled.GameMessages.MainMenu.*;
import com.ExceptionHandled.GameMessages.Stats.GameHistoryDetail;
import com.ExceptionHandled.GameMessages.Stats.GameHistorySummary;
import com.ExceptionHandled.GameMessages.Stats.PlayerStatsInfo;
import com.ExceptionHandled.GameMessages.UserInfo;
import com.ExceptionHandled.GameMessages.UserUpdate.UserDeleteFail;
import com.ExceptionHandled.GameMessages.UserUpdate.UserDeleteSuccess;
import com.ExceptionHandled.GameMessages.UserUpdate.UserUpdateRequest;
import com.ExceptionHandled.GameMessages.UserUpdate.UserUpdateSuccess;
import com.ExceptionHandled.GameMessages.Wrappers.Packet;
import com.ExceptionHandled.GameServer.Player;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLiteQuery {

    private static SQLiteQuery instance = new SQLiteQuery();
    private Connection connection;

    private SQLiteQuery(){

    }

    public static SQLiteQuery getInstance() {

        return instance;
    }

    public void setConnection(){
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:TicTacToe.db");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private boolean isSignUpIDUnique(String id) throws SQLException {
        PreparedStatement prep = connection.prepareStatement(
                "select count(playerID) from playerInfo where playerID = ?");
        prep.setString(1, id);
        ResultSet rs = prep.executeQuery();
        if(rs.getInt(1) > 0){
            return false;
        }
        return true;
    }

    private boolean isUsernameUnique(String username) throws SQLException {
        PreparedStatement p = connection.prepareStatement("select count(username)from playerInfo where username = ?");
        p.setString(1, username);
        ResultSet r = p.executeQuery();
        if(r.getInt(1) > 0){
            return false;
        }
        return true;
    }

    public Packet insertNewUser(Packet packet) {
        SignUpRequest request = (SignUpRequest)packet.getMessage();

        String id = UUID.randomUUID().toString().substring(0,8);
        try{
            //check if username already exist
            if(!isUsernameUnique(request.getUsername())){
                return new Packet("Login", null, new SignUpFail("Username already exist"));
            }

            while(!isSignUpIDUnique(id)){
                id = UUID.randomUUID().toString().substring(0,8);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            PreparedStatement prep = connection.prepareStatement("INSERT INTO playerInfo values(?,?,?,?,?,?)");
            prep.setString(1, id);
            prep.setString(2,request.getUsername());
            prep.setString(3,request.getPassword());
            prep.setString(4,request.getFirstName());
            prep.setString(5,request.getLastName());
            prep.setBoolean(6,true);
            prep.execute();

            PreparedStatement prep2 = connection.prepareStatement("INSERT INTO playerStats(playerID) values(?)");
            prep2.setString(1, id);
            prep2.execute();

            return new Packet("Login", id, new SignUpSuccess());
        }

        catch (SQLException e) {
            e.printStackTrace();
        }

        return new Packet("Login", "", new SignUpFail("Unknown SignUp Error"));
    }

    public Packet userLoggingIn(Packet packet){
        LoginRequest loginRequest = (LoginRequest)packet.getMessage();

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        ResultSet rs;

        try{
            Statement statement = connection.createStatement();
            rs = statement.executeQuery("Select * from playerInfo");

            while(rs.next()){
                String playerID = rs.getString(1);
                String dbUsername = rs.getString(2);
                String dbPassword = rs.getString(3);
                boolean isActive = rs.getBoolean("isActive");

                if(username.equals(dbUsername) && password.equals(dbPassword)){
                    if(!isActive){
                        return new Packet("Login", playerID, new LoginFail("Account inactive"));
                    }
                    System.out.println("Login Success");
                    return new Packet("Login",playerID, new LoginSuccess(playerID));
                }
                else if(username.equals(dbUsername) && !password.equals(dbPassword)){
                    System.out.println("Incorrect password");
                    return new Packet("Login", "", new LoginFail("Wrong password"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new Packet("Login", "", new LoginFail("Unknown login error"));
    }

    public Packet joinGame(Packet packet){
        String playerID = packet.getPlayerID();

        JoinGameRequest joinGameRequest = (JoinGameRequest)packet.getMessage();
        String gameID = joinGameRequest.getGameId();

        ResultSet rs = null;

        try{
            PreparedStatement prep = connection.prepareStatement("Select * from gameList");
            rs = prep.executeQuery();

            while(rs.next()){
                if(rs.getString(1).equals(gameID) && rs.getString(5).equals("")){
                    PreparedStatement prep1 = connection.prepareStatement(
                            "UPDATE gameList SET player2ID = ? " +
                                    "WHERE gameID = ? ");
                    prep1.setString(1, playerID);
                    prep1.setString(2, gameID);
                    prep1.executeUpdate();

                    PreparedStatement preparedStatement = connection.prepareStatement(
                            "select gameID, gl.gameName, pi.playerID, pi.firstname, pi.lastname\n" +
                                    "from gameList gl\n" +
                                    "join playerInfo pi \n" +
                                    "ON gl.player1ID = pi.playerID");
                    ResultSet resultSet = preparedStatement.executeQuery();
                    String opponentPlayerName = resultSet.getString(4) + " " + resultSet.getString(5);
                    String gameName = resultSet.getString(2);

                    PreparedStatement prep2 = connection.prepareStatement(
                            "select * from moveList mL where mL.gameID = ?");
                    prep2.setString(1, gameID);
                    ResultSet moveSet = prep2.executeQuery();

                    ArrayList<MoveValid> moveList = new ArrayList<>(9);
                    while(moveSet.next()){
                        MoveValid m = new MoveValid(gameID, moveSet.getString(3), moveSet.getInt(4), moveSet.getInt(5));
                        moveList.add(m);
                    }

                    return new Packet("MainMenu", playerID, new JoinGameSuccess(gameID, gameName, opponentPlayerName, moveList));
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return new Packet("MainMenu", playerID, new JoinGameFail("Unknown join game error"));
    }

    public Packet getGameHistoryDetailForPlayer(Packet packet, String gameID){
        String playerID = packet.getPlayerID();

        try {
            PreparedStatement prep = connection.prepareStatement("SELECT * FROM moveList mL where mL.gameID = ?");
            prep.setString(1, gameID);
            ResultSet rs = prep.executeQuery();

            List<MoveValid> moveList = new ArrayList<>();
            while(rs.next()){
                MoveValid m = new MoveValid(gameID, rs.getString(3), rs.getInt(4), rs.getInt(5));
                moveList.add(m);
            }

            PreparedStatement prep1 = connection.prepareStatement("select * from gameList gL where gL.gameID = ?");
            prep1.setString(1, gameID);
            ResultSet gameSet = prep1.executeQuery();

            int winner = gameSet.getInt(6);
            String player = packet.getPlayerID();
            String p1 = gameSet.getString(4);
            String p2 = gameSet.getString(5);
            String matchResult;
            if (winner == 3) {
                matchResult = "Tie";
            }
            else if ((player.equalsIgnoreCase(p1) && winner == 1) || (player.equalsIgnoreCase(p2) && winner == 2)) {
                matchResult = "Win";
            }
            else {
                matchResult = "Loss";
            }
            GameHistorySummary gameHistorySummary = new GameHistorySummary(gameID, p1, p2, matchResult);
            java.sql.Date startDate = gameSet.getDate(2);
            java.sql.Date endDate = gameSet.getDate(3);

            java.util.Date sDate = new Date(startDate.getTime());
            java.util.Date eDate = new Date(endDate.getTime());

            GameHistoryDetail detail = new GameHistoryDetail(gameHistorySummary, sDate, eDate, moveList, null);
            return new Packet("Stats", playerID, detail);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Packet("Stats", playerID, null);
    }

    public GameHistoryDetail getGameDetail(String gameID){
        try {
            PreparedStatement prep = connection.prepareStatement("SELECT * FROM gameList WHERE gameID = ?");
            prep.setString(1, gameID);
            ResultSet gameRS = prep.executeQuery();
            gameRS.next();

            Date sqlStartDate = gameRS.getDate("startTime");
            java.util.Date utilStartDate = new Date(sqlStartDate.getTime());
            Date sqlEndDate = gameRS.getDate("endTime");
            java.util.Date utilEndDate;
            if(sqlEndDate == null){
                utilEndDate = new java.util.Date(0);
            }
            else{
                utilEndDate = new Date(sqlEndDate.getTime());
            }

            //TODO: process match result
//            int matchResult = gameRS.getInt(6);
//            String result;
//            switch(matchResult){
//                case 0:
//                    result = ""
//            }
            System.out.println("int match result: " + gameRS.getInt(6));
            String result = String.valueOf(gameRS.getInt(6));

            GameHistorySummary ghSummary = new GameHistorySummary(gameID, gameRS.getString(4), gameRS.getString(5), result, gameRS.getString("gameName"),
                    utilStartDate, utilEndDate);

            PreparedStatement prep1 = connection.prepareStatement("SELECT playerID, x_coord, y_coord, time from moveList WHERE gameID = ?");
            prep1.setString(1,gameID);
            ResultSet moveRS = prep1.executeQuery();
            List<MoveValid> moveValids = new ArrayList<>();
            while(moveRS.next()){
                MoveValid mv = new MoveValid(gameID, moveRS.getString("playerID"), moveRS.getInt("x_coord"),
                        moveRS.getInt("y_coord"), moveRS.getDate("time"));
                moveValids.add(mv);
            }

            PreparedStatement prep2 = connection.prepareStatement("SELECT viewers.userID, playerInfo.username, playerInfo.firstname, playerInfo.lastname\n" +
                    "FROM viewers\n" +
                    "JOIN playerInfo ON viewers.userID = playerInfo.playerID\n" +
                    "WHERE gameID = ?");
            prep2.setString(1, gameID);
            ResultSet viewerRS = prep2.executeQuery();
            List<UserInfo> viewers = new ArrayList<>();
            //Password is not included.
            String pw = "";
            while(viewerRS.next()){
                UserInfo viewerInfo = new UserInfo(viewerRS.getString(1), viewerRS.getString(2), pw,
                        viewerRS.getString(3), viewerRS.getString(4));
                viewers.add(viewerInfo);
            }

            GameHistoryDetail detail = new GameHistoryDetail(ghSummary,moveValids,viewers);
            return detail;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<GameHistorySummary> getAllGameHistorySummary(){
        List<GameHistorySummary> gameHistorySummaries = new ArrayList<>();

        try {
            PreparedStatement prep = connection.prepareStatement("SELECT * FROM gameList");
            ResultSet gameRS = prep.executeQuery();
            while(gameRS.next()){
                int matchResult = gameRS.getInt("gameStatus");
                String stat = "No conclusion";
                switch(matchResult){
                    case 1:
                        stat = "player 1 won";
                        break;
                    case 2:
                        stat = "player 2 won";
                        break;
                    case 3:
                        stat = "draw";
                }

                GameHistorySummary ghs = new GameHistorySummary(gameRS.getString("gameID"), gameRS.getString("player1ID"),
                        gameRS.getString("player2ID"), stat, gameRS.getString("gameName"), gameRS.getDate("startTime"),
                        gameRS.getDate("endTime"));
                gameHistorySummaries.add(ghs);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gameHistorySummaries;
    }

    public Packet getPlayerStatsInfo(Packet packet){
        String playerID = packet.getPlayerID();

        try {
            PreparedStatement prep = connection.prepareStatement("SELECT * from playerStats where playerID = ?");
            prep.setString(1, playerID);
            ResultSet rs = prep.executeQuery();

            int win = 0;
            int loss = 0;
            int draw = 0;

            while(rs.next()){
                win = rs.getInt(3);
                loss = rs.getInt(4);
                draw = rs.getInt(5);
            }

            //Get list of games this player played
            List<GameHistorySummary> gameHistorySummaries = new ArrayList<>();

            PreparedStatement prep1 = connection.prepareStatement("Select * from gameList where player1ID = ?");
            prep1.setString(1, playerID);
            ResultSet rs1 = prep1.executeQuery();

            while(rs1.next()){
                gameHistorySummaries.add(new GameHistorySummary(
                        rs1.getString(1),
                        playerID,
                        rs1.getString(5),
                        rs1.getString(6)));
            }

            //Now get games where this player is player2
            PreparedStatement prep2 = connection.prepareStatement("Select * from gameList where player2ID = ?");
            prep2.setString(1, playerID);
            ResultSet rs2 = prep2.executeQuery();
            while(rs2.next()){
                gameHistorySummaries.add(new GameHistorySummary(
                        rs2.getString(1),
                        rs2.getString(4),
                        playerID,
                        rs2.getString(6)));
            }

            PlayerStatsInfo info = new PlayerStatsInfo(win, loss, draw, gameHistorySummaries);
            return new Packet("Stats", playerID, info);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new Packet("Stats", playerID, null);
    }

    public Player getPlayerDetail(String id){
        Player p = null;
        try {
            PreparedStatement prep = connection.prepareStatement("SELECT playerID, username, firstname, lastname, isActive FROM playerInfo WHERE playerID = ?");
            prep.setString(1, id);
            ResultSet rs = prep.executeQuery();

            rs.next();
            p = new Player(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return p;
    }

    public List<UserInfo> getAllPlayersInfo(){
        List<UserInfo> allPlayers = new ArrayList<>();
        try {
            PreparedStatement prep = connection.prepareStatement("SELECT playerID, username, password, firstname, lastname, isActive\n" +
                    "FROM playerInfo");
            ResultSet playersRS = prep.executeQuery();


            while(playersRS.next()){
                Boolean b = playersRS.getBoolean(6);

                UserInfo user = new UserInfo(playersRS.getString(1), playersRS.getString(2),
                        playersRS.getString(3), playersRS.getString(4), playersRS.getString(5), b);
                allPlayers.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return allPlayers;
    }

    public Packet insertViewerToGame(Packet packet){
        String playerID = packet.getPlayerID();
        SpectateRequest sp = (SpectateRequest)packet.getMessage();

        try {
            PreparedStatement prep = connection.prepareStatement("INSERT INTO viewers(gameID, userID) values(?,?)");
            prep.setString(1, sp.getGameId());
            prep.setString(2, playerID);
            prep.execute();

            //TODO: reimplement this.. perhaps it's better to send SpectateSuccess and put the GameHistoryDetail inside that.
            Packet toReturn = getGameHistoryDetailForPlayer(packet, sp.getGameId());
            return toReturn;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new Packet("MainMenu", playerID, new SpectateFail("Unknown spectate fail error"));
    }

    public void insertMoveHistory(MoveValid moveValid){
        try {
            PreparedStatement prep = connection.prepareStatement("INSERT INTO moveList(gameID, playerID, x_coord, y_coord,time) values (?,?,?,?,?)");
            prep.setString(1, moveValid.getGameID());
            prep.setString(2, moveValid.getPlayer());
            prep.setInt(3,moveValid.getxCoord());
            prep.setInt(4, moveValid.getyCoord());

            //add date to move history, need to test!
            Date date = new Date(new java.util.Date().getTime());
            prep.setDate(5, date);

            prep.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateGameOver(String gameID, int gameStatus, String p1ID, String p2ID){
        //1: player1Won, 2:player2Won, 3: draw
        java.util.Date endDate = new java.util.Date();
        java.sql.Date date2 = new Date(endDate.getTime());

        try {
            PreparedStatement prep = connection.prepareStatement("UPDATE gameList SET gameStatus = ?, endTime = ? WHERE gameID = ?");
            prep.setString(3, gameID);
            prep.setInt(1, gameStatus);
            prep.setDate(2, date2);
            prep.execute();

            if(gameStatus == 1 || gameStatus == 2){
                //player1 won
                PreparedStatement ps = connection.prepareStatement("UPDATE playerStats SET win = win + 1 WHERE playerID = ?");
                PreparedStatement ps2 = connection.prepareStatement("UPDATE playerStats SET lose = lose + 1 WHERE playerID = ?");

                if(gameStatus == 1){
                    ps.setString(1, p1ID);
                    ps2.setString(1,p2ID);
                }
                else{
                    ps.setString(1, p2ID);
                    ps2.setString(1,p1ID);
                }
                ps.execute();
                ps2.execute();
            }
            else{
                //game draw
                PreparedStatement preparedStatement =  connection.prepareStatement("UPDATE playerStats set draw = draw + 1 WHERE playerID = ?");
                PreparedStatement preparedStatement2 =  connection.prepareStatement("UPDATE playerStats set draw = draw + 1 WHERE playerID = ?");
                preparedStatement.setString(1, p1ID);
                preparedStatement2.setString(1,p2ID);

                preparedStatement.execute();
                preparedStatement2.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public String getUsername(String userID){
        try {
            PreparedStatement prep = connection.prepareStatement("SELECT username FROM playerInfo where playerID = ?");
            prep.setString(1, userID);
            ResultSet rs = prep.executeQuery();

            String username = "";
            while(rs.next()){
                username = rs.getString(1);
            }

            return username;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Packet insertNewGame(Packet packet){
        String gameID = UUID.randomUUID().toString();
        String player1ID = packet.getPlayerID();
        String player2Type = "";

        NewGameRequest request = (NewGameRequest)packet.getMessage();

        try {
            PreparedStatement prep = connection.prepareStatement("INSERT INTO gameList(gameID, startTime, player1ID, player2ID, gameName) values(?,?,?,?,?)");
            prep.setString(1, gameID);
            prep.setDate(2, new Date(System.currentTimeMillis()));
            prep.setString(3,player1ID);
            if(request.getOpponent().equals("AI")){
                prep.setString(4,"AI");
                player2Type = "AI";
            }
            else{
                prep.setString(4, "");
            }
            prep.setString(5, request.getGameName());
            prep.execute();

            return new Packet("MainMenu", player1ID, new NewGameSuccess(gameID, request.getGameName(), player2Type));
        }
        catch (SQLException e) {
            e.printStackTrace();
            return new Packet("MainMenu", player1ID, new NewGameFail(""));
        }
    }

    public void listUsers(){
        try {
            Statement statement= connection.createStatement();
            ResultSet rs =statement.executeQuery("Select * from playerInfo");

            while(rs.next()){
                String playerID = rs.getString(1);
                String userName = rs.getString(2);
                String password = rs.getString(3);
                String firstName = rs.getString(4);
                String lastName = rs.getString(5);

                System.out.println(playerID + " " + userName + " " + password);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Packet userDelete(Packet packet) {
        String playerID = packet.getPlayerID();

        try{
            PreparedStatement prep = connection.prepareStatement("UPDATE playerInfo SET isActive = ? WHERE playerID = ?");
            prep.setBoolean(1, false);
            prep.setString(2, playerID);
            prep.executeUpdate();

            return new Packet("UserUpdate", playerID, new UserDeleteSuccess());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new Packet("UserUpdate", playerID, new UserDeleteFail("server error"));
    }

    public Packet updateUserInfo(Packet packet) {
        UserUpdateRequest userUpdateRequest = (UserUpdateRequest)packet.getMessage();
        boolean updateStatus = false;
        boolean isUsernameChanged = false;
        boolean isPasswordChanged = false;
        String playerID = packet.getPlayerID();
        System.out.println("playerID from packet: " + playerID);

        try{
            if(!userUpdateRequest.getNewUsername().equals("") && isSignUpIDUnique(userUpdateRequest.getNewUsername())){
                isUsernameChanged = updateUsername(playerID, userUpdateRequest.getNewUsername());
                updateStatus = isUsernameChanged;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(!userUpdateRequest.getNewFirstName().equals("")){
            updateStatus = updateFirstName(playerID, userUpdateRequest.getNewFirstName());
        }
        if(!userUpdateRequest.getNewLastName().equals("")){
            updateStatus = updateLastName(playerID, userUpdateRequest.getNewLastName());
        }

        //TODO: Figure out how to indicate firstname lastname is also changed
        return new Packet("UserUpdate", playerID, new UserUpdateSuccess(isUsernameChanged, isPasswordChanged));

    }

    private boolean updateUsername(String playerID, String newUsername) {
        System.out.println("updateUsername called");
        System.out.println("PlayerID: " + playerID);
        try{
            PreparedStatement prep = connection.prepareStatement("UPDATE playerInfo SET username = ? WHERE playerID = ?");
            prep.setString(1, newUsername);
            prep.setString(2, playerID);
            prep.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateFirstName(String playerID, String newFname){
        try{
            PreparedStatement prep = connection.prepareStatement("UPDATE playerInfo SET firstName = ? WHERE playerID = ?");
            prep.setString(1, newFname);
            prep.setString(2, playerID);
            prep.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateLastName(String playerID, String newLname){
        try{
            PreparedStatement prep = connection.prepareStatement("UPDATE playerInfo SET lastName = ? WHERE playerID = ?");
            prep.setString(1, newLname);
            prep.setString(2, playerID);
            prep.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
