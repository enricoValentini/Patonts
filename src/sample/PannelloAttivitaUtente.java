package sample;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

public class PannelloAttivitaUtente {


    @FXML
    private Button goBackBtn;

    @FXML
    private Label nomeECognome;

    @FXML
    private Label closeLabel;

    @FXML
    private Label minimizeLabel;

    @FXML
    private ImageView logoutBtn;

    @FXML
    private ImageView exportBtn;

    @FXML
    private ImageView importBtn;

    @FXML
    private GridPane attivitaTable;
    private int column = 0;
    private int row = 0;

    @FXML
    private Label fileNotFoundLabel;

    private String Nwks;
    private Utente user;
    private Workspace w;
    private String workspace;
    private ArrayList<String> attivita;
    private double xOffset = 0;
    private double yOffset = 0;
    private String usersFilePath = "users.json";
    private String workspacesFilePath = "workspaces.json";




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

    public void showInfo(String workspace,ArrayList<String> listattivita, Utente user, String usersFile, String workspacesFile, String Nwks){
        this.Nwks = Nwks;
        this.workspace = workspace;
        this.attivita = listattivita;
        this.user = user;
        this.nomeECognome.setText(this.user.getNome() + " " + this.user.getCognome()+ " /Workspaces / "+Nwks);
        this.usersFilePath = usersFile;
        this.workspacesFilePath = workspacesFile;
    }

    public void setNwks(String n){
        this.Nwks = n;
    }




    @FXML

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

    //Carica tutte le attivita del wks
    public void caricaAttivita(){
        this.attivitaTable.setAlignment(Pos.CENTER);
        ArrayList<Attivita> workspacesAmministratore = new ArrayList<Attivita>();
        workspacesAmministratore = getAttivita(user.getUsername());  //POSSSO PASSARE DIRETTAMENTE ATTIVITA E WORKSPACE

        if(workspacesAmministratore.size() == 0) {
            this.fileNotFoundLabel.setText("Non hai ancora nessun workspace. Creane qualcuno");
            this.fileNotFoundLabel.setVisible(true);
            return;
        }
        for(Attivita a : workspacesAmministratore)
            addWorkspaceToGridPane(a, user);

    }

    //recupera i eks del json
    protected ArrayList<Attivita> getAttivita(String userName){  //POSSSO RICAVARE DIRETTAMENTE WORKSPACE E ATTIVITA

        ArrayList<Attivita> att = new ArrayList<Attivita>(); //INIZIALIZZO ARRAY DI PREFERENZE VUOTO

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            JsonReader reader = new JsonReader(new FileReader(workspacesFilePath)); //PASSO IL FILE WORKSPACE
            List<Workspace> workspaces = new Gson().fromJson(reader, new TypeToken<List<Workspace>>(){}.getType());

                            for (Workspace w : workspaces) {

                                if (w.getID().equals(workspace)) { // CERCO LA CORRISPONDEZA ID WORSPACE

                                    List<Attivita> activ = w.getListAttivita(); //MI SALVO LE ATTIVITA PRESENTI NEL WORSPACE
                                    for (Attivita a : activ) { //CERCO LA CORRISPONDENZA DI ID ATTIVITA
                                        if (attivita.contains(a.getID())) {
                                            att.add(a);

                                           }
                                        }
                                    }
                                }
            reader.close();
            return att;

        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

    //Aggiunge le attivita al GridPane
    private void addWorkspaceToGridPane(Attivita a, Utente u){
        StackPane workspace = createGraphicWorkspace(a, u);
        this.attivitaTable.add(workspace, column, row, 1, 1);
        if(column < this.attivitaTable.getColumnCount() - 1)
            column ++;
        else {
            column = 0;
            if(row < this.attivitaTable.getRowCount() - 1)
                row ++;
        }
    }

    //Crea graficamente le attivita
    private StackPane createGraphicWorkspace(Attivita a, Utente u){
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
                try {

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("EditAttivitaUtente.fxml"));
                    Parent root = loader.load();
                    EditAttivitaUtente gestioneAttivita = loader.getController();

                    gestioneAttivita.showInfo(workspace,a.getID(), u, usersFilePath, workspacesFilePath, Nwks);
                    gestioneAttivita.getAttivita(workspace,a.getID());
                   // gestioneAttivita.fillDate();

                    Stage stage = (Stage)workspacePane.getScene().getWindow();
                    stage.setScene(new Scene(root));

                    trascinamentoFinestra(root, stage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Label title = new Label(a.getDescrizione());
        title.setFont(Font.font("System", FontWeight.BOLD, 21));
        title.setAlignment(Pos.CENTER);
        title.setTextFill(Color.WHITE);
        workspacePane.getChildren().add(title);
        return workspacePane;
    }


    @FXML

    void doImport(MouseEvent event) throws IOException {
        Stage stage = (Stage) importBtn.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PannelloImport.fxml"));
        Parent root = loader.load();

        ImportController importController = loader.getController();
        importController.showInfo(user, usersFilePath, workspacesFilePath);

        Scene importScene = new Scene(root);
        stage.setScene(importScene);

        this.trascinamentoFinestra(root, stage);
    }

    @FXML

    void doExport(MouseEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("PannelloBackup.fxml"));
        Parent backupScene = loader.load();

        BackupController backupController = loader.getController();
        backupController.showInfo(user, usersFilePath, workspacesFilePath);

        Stage stage = (Stage)exportBtn.getScene().getWindow();
        stage.setScene(new Scene(backupScene));

        this.trascinamentoFinestra(backupScene, stage);

    }

    @FXML
    void goBack(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PannelloWorkspaceUtente.fxml"));

        Parent root = loader.load();
        PannelloWorkspaceUtente workspaceUtente = loader.getController();

        workspaceUtente.showInfo( user, usersFilePath, workspacesFilePath);
        workspaceUtente.caricaWorkspaces();
        Stage stage = (Stage)goBackBtn.getScene().getWindow();
        stage.setScene(new Scene(root));

        this.trascinamentoFinestra(root, stage);
    }
}

