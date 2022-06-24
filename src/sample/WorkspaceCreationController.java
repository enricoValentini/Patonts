package sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WorkspaceCreationController {
    @FXML
    private Label closeLabel;

    @FXML
    private Label minimizeLabel;

    @FXML
    private Label nomeECognome;

    @FXML
    private TextArea descrizioneWorkspace;

    @FXML
    private TextField idWorkspace;

    @FXML
    private Button createWorkspaceBtn;

    @FXML
    private Button undoBtn;

    @FXML
    private Label errorLabel;

    private Utente admin;
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
    //Crea un nuovo workspace e lo carico sul file json esportato per ultimo

    void creaWorkspace(MouseEvent event) throws IOException{
        Workspace wrk = new Workspace(idWorkspace.getText(), descrizioneWorkspace.getText());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if(verificaIdDoppi(gson, wrk)) {
            this.idWorkspace.setText(null);
            this.errorLabel.setText("Esiste già un workspace con questo ID. Provane un altro!");
            this.errorLabel.setVisible(true);
            return;
        }

        if(this.idWorkspace.getText() == "" || this.descrizioneWorkspace.getText() == ""){
            this.errorLabel.setText("I campi non possono essere vuoti!");
            this.errorLabel.setVisible(true);
            return;
        }

        try {
            //Deserializzo in una lista dinamica
            JsonReader reader = new JsonReader(new FileReader(workspacesFilePath));
            TypeToken<List<Workspace>> token = new TypeToken<List<Workspace>>() {};
            List<Workspace> workspaces = gson.fromJson(reader, token.getType());

            workspaces.add(wrk);
            //Reinserisco nel file json
            //Anche l' oggetto Workspace appena creato

            FileWriter writer = new FileWriter(workspacesFilePath);
            gson.toJson(workspaces, writer);
            writer.close();
        } catch (FileNotFoundException e) {
            //Error label
        } catch (IOException e) {
            e.printStackTrace();
        }

        aggiornaFileUtenti(wrk.getID());
        this.tornaInterfacciaAdmin((Stage) createWorkspaceBtn.getScene().getWindow());

    }

    @FXML
    void minimizeWindow(MouseEvent event) {
        Stage stage = (Stage) minimizeLabel.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    //Annula la creazione del workspace
    //Torno all' interfaccia amministratore
    void undoCreate(MouseEvent event) throws IOException {
        this.tornaInterfacciaAdmin((Stage) undoBtn.getScene().getWindow());
    }

    //Recupera dalla scena precedente il nome e il cognome dell'utente amministratore
    public void showInfo(Utente admin, String usersFile, String workspacesFile){
        this.admin = admin;
        this.nomeECognome.setText(this.admin.getNome() + " " + this.admin.getCognome());
        this.usersFilePath = usersFile;
        this.workspacesFilePath = workspacesFile;
    }

    //Metodo ausiliario che carica l'interfaccia amministratore
    private void tornaInterfacciaAdmin(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PannelloAdmin.fxml"));
        Parent root = loader.load();

        AdminController adminController = loader.getController();
        adminController.showInfo(admin, usersFilePath, workspacesFilePath);
        adminController.caricaWorkspaces();

        stage.setScene(new Scene(root));

        this.trascinamentoFinestra(root, stage);
    }

    //Metodo ausiliario per verificare che non ci siano workspace con lo stesso ID
    private boolean verificaIdDoppi(Gson gson, Workspace wrk){
        try {
            JsonReader reader = new JsonReader(new FileReader(workspacesFilePath));
            TypeToken<List<Workspace>> token = new TypeToken<List<Workspace>>() {};
            List<Workspace> workspaces = gson.fromJson(reader, token.getType());

            for(Workspace w : workspaces)
                if(w.getID().equals(wrk.getID()))
                    return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return false;

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

    //Aggiungo il workspace id all' elenco di workspace dell' amministratore
    private void aggiornaFileUtenti(String workspaceId){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            JsonReader reader = new JsonReader(new FileReader(usersFilePath));
            TypeToken<List<Utente>> token = new TypeToken<List<Utente>>() {};
            List<Utente> utenti = gson.fromJson(reader, token.getType());
            ArrayList<String> workspaces = new ArrayList<String>();

            for(Utente u : utenti)
                if(admin.getUsername().equals(u.getUsername()) && admin.getPassword().equals(u.getPassword()))
                    u.addToWorkspaceId(idWorkspace.getText()+"x"+"none");
                    admin.addToWorkspaceId(idWorkspace.getText()+"x"+"none");

            try {
                FileWriter writer = new FileWriter(usersFilePath);
                gson.toJson(utenti, writer);
                writer.close();
            } catch (JsonIOException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
