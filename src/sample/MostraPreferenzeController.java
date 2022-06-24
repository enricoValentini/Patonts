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
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class MostraPreferenzeController {

    @FXML
    private Label closeLabel;

    @FXML
    private Label minimizeLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label userInfo;

    @FXML
    private Button goBackBtn;

    @FXML
    private Label attivitaLabel;

    @FXML
    private Label dataPreferita;

    @FXML
    private Label luogoPreferito;


    private Utente admin;
    private Workspace workspace;
    private Attivita attivita;
    private double xOffset = 0;
    private double yOffset = 0;
    private String usersFilePath = "users.json";
    private String workspacesFilePath = "workspaces.json";
    private Preferenza pref;


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

    public void showInfo(Utente admin, Workspace workspace, Attivita attvita, String usersFile, String workspacesFile, Preferenza pref){
        this.admin = admin;
        this.workspace = workspace;
        this.attivita = attvita;
        this.usersFilePath = usersFile;
        this.workspacesFilePath = workspacesFile;
        this.pref = pref;
        this.setUserNameAndSurname();
        this.attivitaLabel.setText(attivita.getDescrizione());
        this.usernameLabel.setText(pref.getUserName());
        this.luogoPreferito.setText(pref.getLuogoPref());
        this.dataPreferita.setText(pref.getDatePref());
    }

    private void setUserNameAndSurname(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            JsonReader reader = new JsonReader(new FileReader(usersFilePath));
            TypeToken<List<Utente>> token = new TypeToken<List<Utente>>() {
            };
            List<Utente> users = gson.fromJson(reader, token.getType());

            for(Utente u : users)
                if(u.getUsername().equals(this.pref.getUserName())) {
                    this.userInfo.setText("Utente -" + u.getNome() + " " + u.getCognome());
                    return;
                }

            reader.close();
        }catch(FileNotFoundException exc){

        }catch(IOException exc){

        }

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

    @FXML
    void goBack(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GestioneAttivita.fxml"));
        Parent root = loader.load();

        GestioneAttivitaController gestioneAttivitaController = loader.getController();
        gestioneAttivitaController.showInfo(admin,workspace,attivita,usersFilePath, workspacesFilePath);
        gestioneAttivitaController.caricaUsers();

        Stage stage = (Stage)goBackBtn.getScene().getWindow();
        stage.setScene(new Scene(root));

        this.trascinamentoFinestra(root, stage);
    }

}

