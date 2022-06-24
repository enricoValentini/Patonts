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

import java.io.*;
import java.util.List;

public class BackupLoginController {

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

    private void doExport(String filePathToSave){
        File file = this.openSaveWindow();
        //Se ho scelto un file procedo con l'operazione
        if (file != null)
            this.saveToJson(file, filePathToSave);
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

    @FXML
    void goBack(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage)goBackBtn.getScene().getWindow();
        LoginController loginController = loader.getController();
        loginController.showInfo(usersFilePath, workspacesFilePath);

        stage.setScene(new Scene(root));

        this.trascinamentoFinestra(root, stage);
    }

    @FXML
    void minimizeWindow(MouseEvent event) {
        Stage stage = (Stage) minimizeLabel.getScene().getWindow();
        stage.setIconified(true);
    }

    public void showInfo(String usersFile, String workspacesFile){
        this.usersFilePath = usersFile;
        this.workspacesFilePath = workspacesFile;
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