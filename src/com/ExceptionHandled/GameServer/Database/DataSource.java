package com.ExceptionHandled.GameServer.Database;

import com.ExceptionHandled.GameMessages.Login.SignUpRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class DataSource implements QueryHandle {

    //lazy instantiation because it throws exception from creating SQL connection

    private static DataSource dataSource = new DataSource();
    private Connection connection;

    private DataSource() {
//        connection = setConnection();
    }

    public static DataSource getInstance() {

        return dataSource;
    }

    public Connection setConnection() throws SQLException {
        String URL = "jdbc:mysql://127.0.0.1:3306/";
        String user = "guest";
        String pw = "mypassword";

        String query = "select * from 4blogin.playerinfo;";
        Connection myConn = DriverManager.getConnection(URL, user, pw);
        return myConn;
    }

    @Override
    public void Insert(Object obj) {
        if(obj instanceof SignUpRequest){
            String id = prepForQuery(UUID.randomUUID().toString());

            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO 4blogin.playerinfo values(");

            SignUpRequest request = (SignUpRequest)obj;
            String username = prepForQuery(request.getUsername());
            String password = prepForQuery(request.getPassword());
            String firstname = prepForQuery(request.getFirstName());
            String lastname = prepForQuery(request.getLastName());

            //TODO:check if id is unique

            sb.append(id + ",");
            sb.append(username + ",");
            sb.append(password + ",");
            sb.append(firstname + "," + lastname + ")");


        }
    }

    public String prepForQuery(String str){
        return "'" + str + "'";
    }
}
