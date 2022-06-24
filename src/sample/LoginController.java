package sample;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

//Import per la serializzazione/deserializzazione di file Json
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import javafx.stage.StageStyle;

import java.io.*;
import java.util.List;

public class LoginController {

    @FXML
    private Label closeLabel;

    @FXML
    private Label minimizeLabel;

    @FXML
    private Button loginButton;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ImageView helpAndCredits;

    @FXML
    private Label loginErrorLabel;

    @FXML
    private ImageView exportBtn;

    @FXML
    private ImageView importBtn;


    private double xOffset = 0;
    private double yOffset = 0;
    private String usersFilePath = "users.json";
    private String workspacesFilePath = "workspaces.json";

    @FXML
    //Chiude la finestra

    void closeWindow(MouseEvent event) {
        Stage stage = (Stage) closeLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    //Riduce a icona la finestra

    void minimizeWindow(MouseEvent mouseEvent) {
        Stage stage = (Stage) minimizeLabel.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    //Apre la finestra di help e credits

    void showHelpAndCredits(MouseEvent event) throws IOException{
        Stage stage = (Stage) helpAndCredits.getScene().getWindow();
        Stage newStage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("Credits.fxml"));
        newStage.setScene(new Scene(root));
        newStage.setX(stage.getX());
        newStage.setY(stage.getY());

        this.trascinamentoFinestra(root, newStage);

        //Quando viene creato il nuovo stage quello di login è disabilitato
        newStage.initStyle(StageStyle.UNDECORATED);
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.setTitle("Help & Credits");
        newStage.showAndWait();
    }

    @FXML
    //Efettua il login
    //Gli utenti sono registrati in un file json (users.json)

    void doLogin(MouseEvent event){
        //Oggetto Gson per deserializzare dal file json
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //Deserializza il file json mettendo tutti gli oggetti in una lista dinamica
        try {
            JsonReader reader = new JsonReader(new FileReader(usersFilePath));
            TypeToken<List<Utente>> token = new TypeToken<List<Utente>>() {};
            List<Utente> utenti = gson.fromJson(reader, token.getType());

            //Verifico che l' utente sia registrato
            String username = usernameField.getText();
            String password = passwordField.getText();

            for(Utente u : utenti){
                if(u.getUsername().equals(username) && u.getPassword().equals(password)) {
                    //Stabilsco se caricare il pannello utente o amministratore
                    Parent root = null;
                    if (u.getAdmin()) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("PannelloAdmin.fxml"));
                        root = loader.load();

                        //Il metodo getController() restituisce l'istanza della classe AdminController
                        AdminController adminController = loader.getController();
                        adminController.showInfo(u, usersFilePath, workspacesFilePath);
                        adminController.caricaWorkspaces();

                    } else {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("PannelloWorkspaceUtente.fxml"));
                        root = loader.load();

                        PannelloWorkspaceUtente userController = loader.getController();
                        userController.showInfo(u, usersFilePath, workspacesFilePath);
                        userController.caricaWorkspaces();
                    }

                    Scene next = new Scene(root);
                    Stage window = (Stage)loginButton.getScene().getWindow();
                    window.setTitle("PATonTS - " + u.getNome() + " " + u.getCognome());
                    window.setScene(next);
                    //Permette di trascinare liberamente la finestra
                    this.trascinamentoFinestra(root, window);
                    break;

                }else{
                    //Messaggio di login errato
                    loginErrorLabel.setText("Username o password non validi. Riprova!");
                    loginErrorLabel.setVisible(true);
                    usernameField.setText(null);
                    passwordField.setText(null);
                }
            }

        }catch(FileNotFoundException exc) {
            loginErrorLabel.setText("File non trovato! Importane uno nuovo");
            loginErrorLabel.setVisible(true);
            exportBtn.setDisable(true);
        }catch (IOException exc) {
            loginErrorLabel.setText("Errore nella lettura del file! Provane un altro");
            loginErrorLabel.setVisible(true);
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

    public void showInfo(String usersFile, String workspacesFile){
        this.usersFilePath = usersFile;
        this.workspacesFilePath = workspacesFile;
    }

    @FXML
    void openExportWindow(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("BackupLogin.fxml"));
        Parent root = loader.load();

        BackupLoginController backupLoginController = loader.getController();
        backupLoginController.showInfo(usersFilePath, workspacesFilePath);

        Stage stage = (Stage)exportBtn.getScene().getWindow();
        stage.setScene(new Scene(root));

        this.trascinamentoFinestra(root, stage);
    }

    @FXML
    void openImportWindow(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ImportLogin.fxml"));
        Parent root = loader.load();

        ImportLoginController importLoginController = loader.getController();
        importLoginController.showInfo(usersFilePath, workspacesFilePath);

        Stage stage = (Stage)importBtn.getScene().getWindow();
        stage.setScene(new Scene(root));

        this.trascinamentoFinestra(root, stage);
    }

}