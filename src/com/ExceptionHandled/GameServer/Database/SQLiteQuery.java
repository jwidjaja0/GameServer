package com.ExceptionHandled.GameServer.Database;

import com.ExceptionHandled.GameMessages.Game.MoveValid;
import com.ExceptionHandled.GameMessages.Login.*;
import com.ExceptionHandled.GameMessages.MainMenu.*;
import com.ExceptionHandled.GameMessages.Stats.GameHistoryDetail;
import com.ExceptionHandled.GameMessages.Stats.GameHistorySummary;
import com.ExceptionHandled.GameMessages.UserUpdate.UserDeleteFail;
import com.ExceptionHandled.GameMessages.UserUpdate.UserDeleteSuccess;
import com.ExceptionHandled.GameMessages.UserUpdate.UserUpdateRequest;
import com.ExceptionHandled.GameMessages.UserUpdate.UserUpdateSuccess;
import com.ExceptionHandled.GameMessages.Wrappers.Packet;


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


    public Packet insertViewerToGame(Packet packet){
        String playerID = packet.getPlayerID();
        SpectateRequest sp = (SpectateRequest)packet.getMessage();

        try {
            PreparedStatement prep = connection.prepareStatement("INSERT INTO viewers(gameID, userID) values(?,?)");
            prep.setString(1, sp.getGameId());
            prep.setString(2, playerID);
            prep.execute();


            PreparedStatement prep2 = connection.prepareStatement("select * from moveList mL where mL.gameID = ?");
            prep2.setString(1, sp.getGameId());
            ResultSet moveSet = prep2.executeQuery();

            List<MoveValid> moveList = new ArrayList<>(9);
            while(moveSet.next()){
                MoveValid m = new MoveValid(sp.getGameId(), moveSet.getString(3), moveSet.getInt(4), moveSet.getInt(5));
                moveList.add(m);
            }

            PreparedStatement prep3 = connection.prepareStatement("select * from gameList gL where gL.gameID = ?");
            prep3.setString(1, sp.getGameId());
            ResultSet gameSet = prep3.executeQuery();

            GameHistorySummary gameHistorySummary = new GameHistorySummary(sp.getGameId(), gameSet.getString(4), gameSet.getString(5), gameSet.getInt(6));
            java.sql.Date startDate = gameSet.getDate(2);
            java.sql.Date endDate = gameSet.getDate(3);

            java.util.Date sDate = new Date(startDate.getTime());
            java.util.Date eDate = new Date(endDate.getTime());

            GameHistoryDetail detail = new GameHistoryDetail(gameHistorySummary, sDate, eDate, moveList, null);
            return new Packet("GameHistoryDetail", playerID, detail);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new Packet("MainMenu", playerID, new SpectateFail("Unknown spectate fail error"));
    }

    public void insertMoveHistory(MoveValid moveValid){
        try {
            PreparedStatement prep = connection.prepareStatement("INSERT INTO moveList(gameID, playerID, x_coord, y_coord) values (?,?,?,?)");
            prep.setString(1, moveValid.getGameID());
            prep.setString(2, moveValid.getPlayer());
            prep.setInt(3,moveValid.getxCoord());
            prep.setInt(4, moveValid.getyCoord());

            prep.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public Packet insertNewGame(Packet packet){
        String gameID = UUID.randomUUID().toString();
        String player1ID = packet.getPlayerID();

        NewGameRequest request = (NewGameRequest)packet.getMessage();

        try {
            PreparedStatement prep = connection.prepareStatement("INSERT INTO gameList(gameID, startTime, player1ID, player2ID, gameName) values(?,?,?,?,?)");
            prep.setString(1, gameID);
            prep.setDate(2, new Date(System.currentTimeMillis()));
            prep.setString(3,player1ID);
            if(request.getOpponent().equals("Ai")){
                prep.setString(4,"Ai");
            }
            else{
                prep.setString(4, "");
            }
            prep.setString(5, request.getGameName());
            prep.execute();

            return new Packet("MainMenu", player1ID, new NewGameSuccess(gameID, request.getGameName()));
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

        if(!userUpdateRequest.getNewPassword().equals("")){
            isPasswordChanged = updatePassword(playerID, userUpdateRequest.getNewPassword());
            updateStatus = isPasswordChanged;
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

    private boolean updatePassword(String playerID, String newPassword){
        System.out.println("updatePassword called");
        try{
            PreparedStatement prep = connection.prepareStatement("UPDATE playerInfo SET password = ? WHERE playerID = ?");
            prep.setString(1, newPassword);
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
