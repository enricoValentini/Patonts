package sample;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

//Import per la serializzazione/deserializzazione di file Json
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AdminController {

    @FXML
    private Label nomeECognome;

    @FXML
    private Label closeLabel;

    @FXML
    private Label minimizeLabel;

    @FXML
    private ImageView logoutBtn;

    @FXML
    private ImageView addWorkspaceBtn;

    @FXML
    private ImageView exportBtn;

    @FXML
    private ImageView importBtn;

    @FXML
    private GridPane workspaceTable;
    private int column = 0;
    private int row = 0;

    @FXML
    private Label fileNotFoundLabel;

    private Utente admin;
    private double xOffset = 0;
    private double yOffset = 0;
    private String usersFilePath = "users.json";
    private String workspacesFilePath = "workspaces.json";

    @FXML
    //Porta ad una scena che permette di creare un nuovo workspace
    void addWorkspace(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("CreatoreWorkspace.fxml"));
        Parent root = loader.load();

        WorkspaceCreationController wrkController = loader.getController();
        wrkController.showInfo(admin, usersFilePath, workspacesFilePath);

        Scene next = new Scene(root);
        Stage window =(Stage) addWorkspaceBtn.getScene().getWindow();
        window.setTitle("PATonTS - " + admin.getNome() + " " + admin.getCognome());
        window.setScene(next);

        //Permette di trascinare liberamente la finestra
        this.trascinamentoFinestra(root, window);
    }


    @FXML
    void closeWindow(MouseEvent event) {
        Stage stage = (Stage) closeLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    void minimizeWindow(MouseEvent event) {
        Stage stage = (Stage) minimizeLabel.getScene().getWindow();
        stage.setIconified(true);
    }

    //Recupera dalla scena precedente il nome e il cognome dell'utente amministratore
    public void showInfo(Utente admin, String usersFile, String workspacesFile){
        this.admin = admin;
        this.nomeECognome.setText(this.admin.getNome() + " " + this.admin.getCognome());
        this.usersFilePath = usersFile;
        this.workspacesFilePath = workspacesFile;
    }

    @FXML
    //Apre una finestra per l' import dei file
    //L' utente sceglie quale file importare
    void doImport(MouseEvent event) throws IOException {
        Stage stage = (Stage) importBtn.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PannelloImport.fxml"));
        Parent root = loader.load();

        ImportController importController = loader.getController();
        importController.showInfo(admin, usersFilePath, workspacesFilePath);

        Scene importScene = new Scene(root);
        stage.setScene(importScene);

        this.trascinamentoFinestra(root, stage);
    }

    @FXML
    //Effettua il logout
    //Torno alla schermata di login

    void doLogout(MouseEvent event) throws IOException {
        this.nomeECognome.setText(null);
        Stage stage = (Stage) logoutBtn.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent login = loader.load();

        LoginController loginController = loader.getController();
        loginController.showInfo(usersFilePath, workspacesFilePath);

        Scene loginScene = new Scene(login);
        stage.setTitle("PATonTS - Login");
        stage.setScene(loginScene);

        //Permette di trascinare liberamente la finestra
        this.trascinamentoFinestra(login, stage);
    }

    @FXML
    //Apre una finestra per effettuare backup di un file a scelta

    void doExport(MouseEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("PannelloBackup.fxml"));
        Parent backupScene = loader.load();

        BackupController backupController = loader.getController();
        backupController.showInfo(admin, usersFilePath, workspacesFilePath);

        Stage stage = (Stage)exportBtn.getScene().getWindow();
        stage.setScene(new Scene(backupScene));

        this.trascinamentoFinestra(backupScene, stage);

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

    //Carica tutti i workspace creati dall' amministratore
    public void caricaWorkspaces(){
        this.workspaceTable.setAlignment(Pos.CENTER);
        ArrayList<String> workspacesAmministratore = new ArrayList<String>();
        try{
            workspacesAmministratore = getWorkspacesAmministratore();

            if(workspacesAmministratore.size() == 0) {
                this.fileNotFoundLabel.setText("Non hai ancora nessun workspace. Creane qualcuno");
                this.fileNotFoundLabel.setVisible(true);
                return;
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            JsonReader reader = new JsonReader(new FileReader(workspacesFilePath));
            TypeToken<List<Workspace>> token = new TypeToken<List<Workspace>>() {};
            List<Workspace> workspaces = gson.fromJson(reader, token.getType());
            for(String wrk : workspacesAmministratore)
                for(Workspace w : workspaces)
                    if(wrk.equals(w.getID()))
                        addWorkspaceToGridPane(w, admin);

        }catch(FileNotFoundException exc){
            //Disabilito le funzioni di creazione workspace e upload dei dati
            this.fileNotFoundLabel.setText("File non trovato. Carica un file");
            this.fileNotFoundLabel.setVisible(true);
            this.addWorkspaceBtn.setDisable(true);
            this.exportBtn.setDisable(true);
        }
    }

    //Metodo ausiliario che restituisce la lista di workspace gestiti da questo amministratore
    private ArrayList<String> getWorkspacesAmministratore() throws FileNotFoundException{
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        ArrayList<String> adminWorkspacesId = this.admin.getWorkspacesList();
        String[] temp;
        ArrayList<String> als = new ArrayList<>();

        for(String s: adminWorkspacesId){
            temp=s.split("x");
            if(!als.contains(temp[0]))
                als.add(temp[0]);
        }

        return als;
    }

    //Aggiunge il workspace al GridPane
    private void addWorkspaceToGridPane(Workspace w, Utente u){
        StackPane workspace = createGraphicWorkspace(w, u);
        this.workspaceTable.add(workspace, column, row, 1, 1);
        if(column < this.workspaceTable.getColumnCount() - 1)
            column ++;
        else {
            column = 0;
            if(row < this.workspaceTable.getRowCount() - 1)
                row ++;
        }
    }

    //Crea graficamento il workspace
    private StackPane createGraphicWorkspace(Workspace w, Utente u){
        StackPane workspacePane = new StackPane();
        workspacePane.setPrefWidth(90);
        workspacePane.setPrefHeight(20);
        workspacePane.setStyle("-fx-background-color:#21476e; -fx-border-color:#ffffff");
        workspacePane.setAlignment(Pos.CENTER);
        workspacePane.setOnMouseEntered(new EventHandler() {
            @Override
            public void handle(Event event) {
                workspacePane.setCursor(Cursor.HAND);
            }
        });

        workspacePane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                //Carico la scena di gestione del workspace
                FXMLLoader loader = new FXMLLoader(getClass().getResource("GestioneWorkspace.fxml"));
                try {
                    Parent root = loader.load();
                    GestioneWorkspaceController gestioneWorkspace = loader.getController();
                    gestioneWorkspace.showInfo(w, u, usersFilePath, workspacesFilePath);
                    gestioneWorkspace.visualizzaAttivita();

                    Stage stage = (Stage)workspacePane.getScene().getWindow();
                    stage.setScene(new Scene(root));

                    trascinamentoFinestra(root, stage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Label title = new Label(w.getDescrizione());
        title.setFont(Font.font("System", FontWeight.BOLD, 21));
        title.setAlignment(Pos.CENTER);
        title.setTextFill(Color.WHITE);
        workspacePane.getChildren().add(title);
        return workspacePane;
    }
}

