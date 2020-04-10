package com.ExceptionHandled.GameServer.Database;

import java.sql.*;

public class SQLiteTest {
    private Connection connection;
    private Statement statement;


    public SQLiteTest() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:UserDB.db");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

    }

    public void listUsers(){

        try {
            this.statement= connection.createStatement();
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
