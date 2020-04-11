package com.ExceptionHandled.GameServer.Database;

import com.ExceptionHandled.GameMessages.Login.*;
import com.ExceptionHandled.GameMessages.Wrappers.Login;

import java.sql.*;
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
        Statement statement = connection.createStatement();

        String query = "Select playerID from playerinfo";
        ResultSet rs = statement.executeQuery(query);

        while(rs.next()){
            if(rs.getString(1).equals(id)){
                return false;
            }
        }
        return true;
    }

    public Login insertNewUser(SignUpRequest request) {

        String id = UUID.randomUUID().toString().substring(0,8);
        try{
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

            Login login = new Login("SignUpSuccess", new SignUpSuccess());
            return login;
        }

        catch (SQLException e) {
            e.printStackTrace();
        }

        return new Login("SignUpFail", new SignUpFail());
    }

    public Login userLoggingIn(LoginRequest loginRequest){

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        ResultSet rs;

        try{
            Statement statement = connection.createStatement();
            rs = statement.executeQuery("Select * from playerInfo");

            while(rs.next()){
                String dbUsername = rs.getString(2);
                String dbPassword = rs.getString(3);

                if(username.equals(dbUsername) && password.equals(dbPassword)){
//                    if(!rs.getBoolean(6)){
//                        return new Login("LoginFail", new LoginFail("Account inactive"));
//                    }
                    System.out.println("Login Success");
                    return new Login("LoginSuccess", new LoginSuccess(rs.getString(1)));
                }
                else if(username.equals(dbUsername) && !password.equals(dbPassword)){
                    System.out.println("Incorrect password");
                    return new Login("LoginFail", new LoginFail(true,false));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Login("LoginFail", new LoginFail());
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


}
