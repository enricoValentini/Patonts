package sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.*;
import java.util.List;

public class BackupController {

    @FXML
    private Label closeLabel;

    @FXML
    private Label minimizeLabel;

    @FXML
    private Pane backupUtenti;

    @FXML
    private Pane backupWorkspaces;

    @FXML
    private Button goBackBtn;

    private String usersFilePath = "users.json";
    private String workspacesFilePath = "workspaces.json";
    private Utente utente;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    void closeWindow(MouseEvent event) {
        Stage stage = (Stage) closeLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    void doExportUtenti(MouseEvent event) {
        this.doExport(usersFilePath);
    }

    @FXML
    void doExportWorkspaces(MouseEvent event) {
        this.doExport(workspacesFilePath);
    }

    //Metodo ausiliario che che apre una finestra per il salvataggio su file
    private void doExport(String filePath){
        //Apro una finestra di salvataggio file
        File file = openSaveWindow();
        //Se ho scelto un file procedo con l'operazione
        if (file != null) {
            this.saveToJson(file, filePath);
        }
    }

    private File openSaveWindow(){
        //Apro una finestra di salvataggio file
        FileChooser fileChooser = new FileChooser();

        //Imposto il filtro di estensione SOLO per i file json
        FileChooser.ExtensionFilter filtro = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(filtro);

        //*** ISTRUZIONE BLOCCANTE! ***
        File file = fileChooser.showSaveDialog(backupWorkspaces.getScene().getWindow());

        return file;
    }

    @FXML
    void goBack(MouseEvent event) throws IOException {
        Parent root = null;

        if(utente.getAdmin()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PannelloAdmin.fxml"));
            root = loader.load();
            AdminController adminController = loader.getController();
            adminController.showInfo(utente, usersFilePath, workspacesFilePath);
            adminController.caricaWorkspaces();
        }else{
            //Carica il pannello utente
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PannelloWorkspaceUtente.fxml"));
            root = loader.load();
            PannelloWorkspaceUtente workspaceUtente = loader.getController();
            workspaceUtente.showInfo(utente, usersFilePath, workspacesFilePath);
            workspaceUtente.caricaWorkspaces();
        }

        Stage stage = (Stage)goBackBtn.getScene().getWindow();
        stage.setScene(new Scene(root));

        this.trascinamentoFinestra(root, stage);
    }

    @FXML
    void minimizeWindow(MouseEvent event) {
        Stage stage = (Stage) minimizeLabel.getScene().getWindow();
        stage.setIconified(true);
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

    //Metodo ausiliario di salvataggio
    private void saveToJson(File file, String filePathToSave){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            JsonReader reader = new JsonReader(new FileReader(filePathToSave));
            TypeToken<List<Object>> token = new TypeToken<List<Object>>() {};
            List<Object> objects = gson.fromJson(reader, token.getType());

            FileWriter writer = new FileWriter(file);
            gson.toJson(objects, writer);
            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println("Errore");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Metodo usato per mantenere le informazioni tra le scene
    public void showInfo(Utente utente, String usersFile, String workspacesFile){
        this.utente = utente;
        usersFilePath = usersFile;
        workspacesFilePath = workspacesFile;
    }
}