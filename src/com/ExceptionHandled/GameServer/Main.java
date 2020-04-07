package com.ExceptionHandled.GameServer;

import java.sql.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args){

//        UUID id = UUID.randomUUID();
//        System.out.println(id);

        Server gameServer = new Server();

        Scanner in = new Scanner(System.in);

//        int a = 1;
//        while(a != 0){
//            a = in.nextInt();
//        }
    }

        public static void main2 (String[]args) throws SQLException {

            Scanner input = new Scanner(System.in);

            //String URL ="jdbc:mysql://localhost:3306/world";
            String URL2 = "jdbc:mysql://127.0.0.1:3306/";
            String user = "guest";
            String pw = "mypassword";
//        String pw = input.nextLine();

//        String query = "use 4blogin; ";
////                "INSERT INTO playerinfo ";
////                "VALUES (default, 'John', 'passw')";

            String query = "select * from 4blogin.playerinfo;";

            String query2 = "insert into 4blogin.playerinfo values(1, 'Ram', 'abcd')";

            //1. Get a connection to the database
            Connection myConn = DriverManager.getConnection(URL2, user, pw);
            //2. Create a statement
            Statement myStatement = myConn.createStatement();
            //3. Execute SQL Query
            //ResultSet myRs = myStatement.executeQuery(query);
            try{
                myStatement.executeUpdate(query2);
            }
            catch(SQLIntegrityConstraintViolationException e){
                System.out.println("Username taken!");
            }

            //4. Process the result set
//        while(myRs.next()){
//            System.out.println(myRs.getString(1) + ", " + myRs.getString(2));
        }
    }

