package com.ExceptionHandled.GameServer;

import java.sql.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws SQLException {

        Scanner input = new Scanner(System.in);

        //String URL ="jdbc:mysql://localhost:3306/world";
        String URL2 = "jdbc:mysql://127.0.0.1:3306/?user=root";
        String user = "root";
        String pw = input.nextLine();

        String query = "use 4blogin; " +
                "INSERT INTO playerinfo " +
                "VALUES (default, 'John', 'passw')";

        //1. Get a connection to the database
        Connection myConn = DriverManager.getConnection(URL2, user, pw);
        //2. Create a statement
        Statement myStatement = myConn.createStatement();
        //3. Execute SQL Query
        myStatement.executeUpdate(query);
//        ResultSet myRs = myStatement.executeQuery(query);
        //4. Process the result set
//        while(myRs.next()){
//            System.out.println(myRs.getString(1) + ", " + myRs.getString(2));
        }
    }

