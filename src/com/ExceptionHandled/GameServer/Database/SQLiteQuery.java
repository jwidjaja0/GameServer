package com.ExceptionHandled.GameServer.Database;

import java.sql.*;

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
            connection = DriverManager.getConnection("jdbc:sqlite:UserDB.db");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.exit(-1);
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


}
