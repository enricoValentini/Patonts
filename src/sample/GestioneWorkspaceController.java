package sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GestioneWorkspaceController {

    @FXML
    private Label workspaceInfo;

    @FXML
    private Label closeLabel;

    @FXML
    private Label minimizeLabel;

    @FXML
    private Label adminInfo;

    @FXML
    private TextArea descrizioneTextArea;

    @FXML
    private Button goBackBtn;

    @FXML
    private Button goBackBtn1;

    @FXML
    private Button modifyBtn;

    @FXML
    private Label errorLabel;

    @FXML
    private Label noActivityLabel;

    @FXML
    private VBox activityList;

    @FXML
    private Button addActivityBtn;


    private Utente admin;
    private Preferenza p;
    private Workspace workspace;
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

    //Carica informazioni del workspace in cui sono entrato
    //E anche i percorsi dei file necessari
    public void showInfo(Workspace w, Utente admin, String usersFile, String workspacesFile){
        this.workspace = w;
        this.admin = admin;
        adminInfo.setText(admin.getNome() + " " + admin.getCognome());
        workspaceInfo.setText("Workspace - " + w.getDescrizione());
        descrizioneTextArea.setText(w.getDescrizione());
        usersFilePath = usersFile;
        workspacesFilePath = workspacesFile;
    }
    public void showInfo(Preferenza p, Utente admin, String usersFile, String workspacesFile){
        this.p = p;
        this.admin = admin;
        adminInfo.setText(admin.getNome() + " " + admin.getCognome());
        workspaceInfo.setText("Workspace - " + p.getDescrizione());
        descrizioneTextArea.setText(p.getDescrizione());
        usersFilePath = usersFile;
        workspacesFilePath = workspacesFile;
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

    //Modifica il nome del workspace
    public void modifyWorkspace(MouseEvent mouseEvent) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            JsonReader reader = new JsonReader(new FileReader(workspacesFilePath));
            TypeToken<List<Workspace>> token = new TypeToken<List<Workspace>>() {};
            List<Workspace> workspaces = gson.fromJson(reader, token.getType());

            for (Workspace w : workspaces) {
                if (w.getID().equals(this.workspace.getID())) {
                    w.setDescrizione(descrizioneTextArea.getText());
                    showInfo(w, admin, usersFilePath, workspacesFilePath);

                    try {
                        FileWriter writer = new FileWriter(workspacesFilePath);
                        gson.toJson(workspaces, writer);
                        writer.close();
                    } catch (JsonIOException e) {
                        System.out.println("JsonException");
                    } catch (IOException e) {
                        System.out.println("IOException");
                    }

                    errorLabel.setText("Workspace modificato con successo!");
                    errorLabel.setTextFill(Color.GREEN);
                    errorLabel.setVisible(true);

                    return;
                }
            }
        }catch(FileNotFoundException exc){
            errorLabel.setText("File non trovato!");
            errorLabel.setTextFill(Color.RED);
            errorLabel.setVisible(true);
        }
    }

    @FXML
    void goBack(MouseEvent mouseEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PannelloAdmin.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage)goBackBtn.getScene().getWindow();

        AdminController adminController = loader.getController();
        adminController.showInfo(admin, usersFilePath, workspacesFilePath);
        adminController.caricaWorkspaces();

        stage.setScene(new Scene(root));

        this.trascinamentoFinestra(root, stage);
    }

    @FXML
    //Apre una finestra per la creazione dell' attività
    void openAddActivityWindow(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("CreatoreAttivita.fxml"));
        Parent root = loader.load();

        ActivityCreationController activityCreatorController = loader.getController();
        activityCreatorController.showInfo(admin, workspace, usersFilePath, workspacesFilePath);

        Stage stage = (Stage)addActivityBtn.getScene().getWindow();
        stage.setScene(new Scene(root));

        this.trascinamentoFinestra(root, stage);
    }

    //Visualizza tutte le attività contenute nel workspace in questione
    public void visualizzaAttivita(){
        ArrayList<Attivita> attivita = new ArrayList<Attivita>();
        attivita = workspace.getListAttivita();

        //Se non ci sono attività visualizza un messaggio di assenza di attivita
        //Altrimenti le visualizzo tutte
        if(attivita.size() == 0)
            this.noActivityLabel.setVisible(true);
        else
            addActivityToVBox(attivita);
    }

    //Visualizza le attivita del workspace
    private void addActivityToVBox(ArrayList<Attivita> attivita){
        for(Attivita a : attivita)
            activityList.getChildren().add(createGraphicActivity(a));
    }

    //Creo graficamente le attivita
    private StackPane createGraphicActivity(Attivita a){
        StackPane activityPane = new StackPane();
        activityPane.setPrefWidth(345);
        activityPane.setPrefHeight(40);
        activityPane.setStyle("-fx-background-color:#21476e; -fx-border-color:#ffffff");
        activityPane.setAlignment(Pos.CENTER);

        activityPane.setOnMouseEntered(new EventHandler() {
            @Override
            public void handle(Event event) {
                activityPane.setCursor(Cursor.HAND);
            }
        });

        activityPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                //Apro una finestra per modificare la mia attività
                FXMLLoader loader = new FXMLLoader(getClass().getResource("GestioneAttivita.fxml"));
                try {
                    Parent root = loader.load();
                    GestioneAttivitaController gestioneAttivitaController = loader.getController();
                    gestioneAttivitaController.showInfo(admin, workspace, a, usersFilePath, workspacesFilePath);
                    gestioneAttivitaController.caricaUsers();
                    Stage stage = (Stage)activityPane.getScene().getWindow();
                    stage.setScene(new Scene(root));

                    trascinamentoFinestra(root, stage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        Label title = new Label(a.getDescrizione());
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setAlignment(Pos.CENTER);
        title.setTextFill(Color.WHITE);
        activityPane.getChildren().add(title);
        return activityPane;

    }

}
