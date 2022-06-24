package sample;

import TeleBot.TeleBot;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.FileNotFoundException;

public class Main extends Application implements Runnable {

    TeleBot telebot = new TeleBot();
    //Variabili
    private double xOffset = 0;
    private double yOffset = 0;
    private String usersFilePath = "users.json";
    private String workspacesFilePath = "workspaces.json";

    @Override
    public void start(Stage primaryStage) throws Exception{
        //Carica il pannello iniziale tramite FXMLLoader
        //Autenticazione
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent login = loader.load();

        LoginController loginController = loader.getController();
        loginController.showInfo(usersFilePath, workspacesFilePath);

        primaryStage.setScene(new Scene(login));
        primaryStage.setTitle("PATonTS - Login");
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
        this.trascinamentoFinestra(login, primaryStage);
    }

    //Main
    public static void main(String[] args) {
        Main main = new Main();
        Thread thread = new Thread(main);
        thread.start();
        launch(args);
        main.telebot.loop = false;
    }

    //Metodo ausiliario che permette di trascinare la finestra (drag & drop)
    private void trascinamentoFinestra(Parent root, Stage window){
        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override

            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }

        });

        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override

            public void handle(MouseEvent event) {
                window.setX(event.getScreenX() - xOffset);
                window.setY(event.getScreenY() - yOffset);
            }

        });
    }

    @Override
    public void run() {
        try {
            telebot.start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
