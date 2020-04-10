package com.ExceptionHandled.GameServer.Database;

import com.ExceptionHandled.GameMessages.Login.*;
import com.ExceptionHandled.GameMessages.Wrappers.Login;

import java.sql.*;
import java.util.UUID;

public class DataQuery implements QueryHandle {

    //lazy instantiation because it throws exception from creating SQL connection
    private static DataQuery instance = new DataQuery();
    private Connection connection;

    private static final String SQCONN = "jdbc:sqlite:playerInfo.sqlite";

    private DataQuery() {
    }

    public static DataQuery getInstance() {

        return instance;
    }

    public void setConnection() {
//        String URL = SQCONN;
//        String user = "guest";
//        String pw = "mypassword";

        try {
            connection = DriverManager.getConnection(SQCONN);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isSignUpIDUnique(String id) throws SQLException {
        Statement statement = connection.createStatement();

        String query = "select playerID from playerInfo";
        ResultSet resultSetID = statement.executeQuery(query);
        while(resultSetID.next()){
            System.out.println(resultSetID.getString(1));
            if(resultSetID.getString(1).equals(id)){
                return false;
            }
        }
        return true;
    }

    public Login InsertNewUser(SignUpRequest request) {
        StringBuilder sb = new StringBuilder();
//        sb.append("INSERT INTO 4blogin.playerinfo values(");
        sb.append("INSERT INTO playerInfo values(");

        String username = prepForQuery(request.getUsername());
        String password = prepForQuery(request.getPassword());
        String firstname = prepForQuery(request.getFirstName());
        String lastname = prepForQuery(request.getLastName());

        String id = UUID.randomUUID().toString().substring(0,6);
        try {
            while (!isSignUpIDUnique(id)) {
                id = UUID.randomUUID().toString().substring(0, 6);
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }

        try {
            sb.append(prepForQuery(id) + ",");
            sb.append(username + ",");
            sb.append(password + ",");
            sb.append(firstname + "," + lastname + ")");

            Statement myStatement = connection.createStatement();
            myStatement.executeUpdate(sb.toString());

            //if query inserted successfully (username is unique)
            Login login = new Login("SignUpSuccess", new SignUpSuccess());
            return login;

        }
        catch(SQLIntegrityConstraintViolationException e){
            System.out.println("Duplicate username!");
            return new Login("SignUpFail", new SignUpFail("Duplicate username"));
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return new Login("SignUpFail", new SignUpFail("unknown error"));
    }

    public Login userLoggingIn(LoginRequest loginRequest){
        StringBuilder sb = new StringBuilder();

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        sb.append("SELECT * FROM 4blogin.playerinfo");
        ResultSet resultSet;

        //Get ResultSet from Database
        try {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(sb.toString());

            while(resultSet.next()){
                if(resultSet.getString(2).equals(username) &&
                        resultSet.getString(3).equals(password)){
                    return new Login("LoginSuccess", new LoginSuccess(resultSet.getString(1)));
                }
                else if(resultSet.getString(2).equals(username) &&
                        resultSet.getString(3) != password){
                    return new Login("LoginFail", new LoginFail("Wrong Password"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new Login("LoginFail", new LoginFail("Unknown cause"));
    }

    @Override
    public void Insert(Object obj) {
        if(obj instanceof SignUpRequest){
        }
    }

    public String prepForQuery(String str){
        return "'" + str + "'";
    }
}
