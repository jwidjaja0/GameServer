package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameServer.Database.SQLiteQuery;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {

        Server gameServer = new Server();
        Application.launch(args);


    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("My app");
        stage.show();
    }
}

