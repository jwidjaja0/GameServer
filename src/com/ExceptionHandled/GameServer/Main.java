package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameServer.Database.SQLiteQuery;
import com.ExceptionHandled.GameServer.Database.SQLiteTest;

import java.sql.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        //erver gameServer = new Server();
        SQLiteQuery.getInstance().setConnection();
        SQLiteQuery.getInstance().listUsers();

    }

    public static void main2(String[] args) {

        SQLiteTest sq = new SQLiteTest();
        sq.listUsers();
    }
}

