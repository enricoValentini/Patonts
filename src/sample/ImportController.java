package sample;

import TeleBot.Response2;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class ImportController {

    @FXML
    private Label closeLabel;

    @FXML
    private Label minimizeLabel;

    @FXML
    private Pane importUtenti;

    @FXML
    private Pane importWorkspaces;

    @FXML
    private Button goBackBtn;

    @FXML
    private Label errorLabel;


    private String usersFilePath = "users.json";
    private String workspacesFilePath = "workspaces.json";
    private Utente utente;
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean importatoFileUtenti = false;


    public void showInfo(Utente utente, String usersFile, String workspacesFile){
        this.utente = utente;
        usersFilePath = usersFile;
        workspacesFilePath = workspacesFile;
    }

    @FXML
    void closeWindow(MouseEvent event) {
        Stage stage = (Stage) closeLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    void doImportUtenti(MouseEvent event) {
        File file = openFileWindow();
        if(file != null) {
            //Controlla se ho caricato il giusto filepath per evitare inconsistenze nel funzionamento dell'applicazione
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            try {
                JsonReader reader = new JsonReader(new FileReader(file));
                TypeToken<List<Utente>> token = new TypeToken<List<Utente>>() {};
                List<Utente> utenti = gson.fromJson(reader, token.getType());

                if (utenti.get(0).getUsername() == null) {
                    this.errorLabel.setText("Il file caricato non è un file di utenti. Riprova!");
                    this.errorLabel.setVisible(true);
                    return;
                }

            } catch (FileNotFoundException exc) {

            } catch (IndexOutOfBoundsException exc) {
                //Il file caricato potrebbe non contenere workspace
            }

            this.usersFilePath = file.getAbsolutePath();
            Response2.changeUserPath(file.getAbsolutePath());
            this.importatoFileUtenti = true;
        }
    }

    @FXML
    void doImportWorkspaces(MouseEvent event) {
        File file = this.openFileWindow();
        //Controlla se ho caricato il giusto filepath per evitare inconsistenze nel funzionamento dell'applicazione
        if (file != null) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            try {
                JsonReader reader = new JsonReader(new FileReader(file));
                TypeToken<List<Workspace>> token = new TypeToken<List<Workspace>>() {
                };
              //  List<Workspace> workspaces = gson.fromJson(reader, token.getType());
                List<Workspace> workspaces = new Gson().fromJson(reader, new TypeToken<List<Workspace>>(){}.getType());


                if (workspaces.get(0).getDescrizione() == null) {
                    this.errorLabel.setText("Il file caricato non è un file di workspace. Riprova!");
                    this.errorLabel.setVisible(true);
                    return;
                }

            } catch (FileNotFoundException exc) {

            } catch (IndexOutOfBoundsException exc) {
                //Il file caricato potrebbe non contenere workspace
            }

            this.workspacesFilePath = file.getAbsolutePath();
            Response2.changeWksPath(file.getAbsolutePath());

        }
    }

    //Apre la finestra di apertura del file dal filesystem
    private File openFileWindow(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filtro = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(filtro);
        File file = fileChooser.showOpenDialog((Stage)importWorkspaces.getScene().getWindow());

        return file;
    }

    @FXML
    void goBack(MouseEvent event) throws IOException {
        Parent root = null;
        Stage stage = (Stage)goBackBtn.getScene().getWindow();

        //Se ho importato il file utenti per evitare incongruenze torno alla pagina di login
        if (importatoFileUtenti){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            root = loader.load();
            LoginController loginController = loader.getController();
            loginController.showInfo(usersFilePath, workspacesFilePath);
            stage.setTitle("PATonTS - Login");
        }else{
            if (utente.getAdmin()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("PannelloAdmin.fxml"));
                root = loader.load();
                AdminController adminController = loader.getController();
                adminController.showInfo(utente, usersFilePath, workspacesFilePath);
                adminController.caricaWorkspaces();
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("PannelloWorkspaceUtente.fxml"));
                root = loader.load();
                PannelloWorkspaceUtente workspaceUtente = loader.getController();
                workspaceUtente.showInfo(utente, usersFilePath, workspacesFilePath);
                workspaceUtente.caricaWorkspaces();
            }
        }

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

}
