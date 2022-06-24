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
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityCreationController {

    @FXML
    private Button addDataBtn;

    @FXML
    private Button addLuogoBtn;

    @FXML
    private ChoiceBox<String> choiceData;

    @FXML
    private ChoiceBox<String> choiceLuogo;

    @FXML
    private Label closeLabel;

    @FXML
    private Label minimizeLabel;

    @FXML
    private Label workspaceInfo;

    @FXML
    private TextArea descrizioneTextArea;

    @FXML
    private TextField idTextField;

    @FXML
    private Button createBtn;

    @FXML
    private Button undoBtn;

    @FXML
    private TextField oraInizioText;

    @FXML
    private TextField oraFineText;

    @FXML
    private Label errorLabel;

    @FXML
    private TextField luogoText;

    @FXML
    private DatePicker datePicker;

    private Utente admin;
    private Workspace workspace;
    private double xOffset = 0;
    private double yOffset = 0;
    private String usersFilePath = "users.json";
    private String workspacesFilePath = "workspaces.json";
    private String[] datePossibili;
    private String[] luoghiPossibili;


    @FXML
    void closeWindow(MouseEvent event) {
        Stage stage = (Stage) closeLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    void createActivity(MouseEvent event) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ArrayList<Attivita> attivita = new ArrayList<Attivita>();
        attivita.addAll(this.workspace.getListAttivita());

        //Controllo se i campi sono stati riempiti
        if(descrizioneTextArea.getText() == "" || idTextField.getText() == "" ||
                oraFineText.getText() == "" || oraInizioText.getText() == ""){
            this.errorLabel.setText("I campi non possono essere vuoti!");
            this.errorLabel.setVisible(true);
            return;
        }

        //Controllo l'inserimento di un ID identico
        if(this.verificaIdDoppi(attivita)){
            this.errorLabel.setText("Esiste già un'attività con questo ID. Provane un altro!");
            idTextField.setText(null);
            this.errorLabel.setVisible(true);
            return;
        }

        //Controllo che i valori delle ore siano compresi tra 0 e 23
        try {
            if ((Integer.parseInt(oraInizioText.getText()) <= 0 && Integer.parseInt(oraInizioText.getText()) >= 23) ||
                    (Integer.parseInt(oraFineText.getText()) <= 0 || Integer.parseInt(oraFineText.getText()) >= 23)){
                oraInizioText.setText(null);
                oraFineText.setText(null);
                this.errorLabel.setText("Formato ore non valido. Riprova!");
                this.errorLabel.setVisible(true);
                return;
            }
        }catch(NumberFormatException exc){
            oraInizioText.setText(null);
            oraFineText.setText(null);
            this.errorLabel.setText("Formato ore non valido. Riprova!");
            this.errorLabel.setVisible(true);
            return;
        }

        //Metto i valori dei choice box in array
        luoghiPossibili = new String[choiceLuogo.getItems().size()];
        for(int i=0; i < luoghiPossibili.length; i++) {
            choiceLuogo.getSelectionModel().select(i);
            this.luoghiPossibili[i] = choiceLuogo.getValue();
        }

        datePossibili = new String[choiceData.getItems().size()];
        for(int i=0; i < datePossibili.length; i++) {
            choiceData.getSelectionModel().select(i);
            this.datePossibili[i] = choiceData.getValue();
        }

        //Creo quindi la mia attivita
        Attivita a = new Attivita(idTextField.getText(), descrizioneTextArea.getText(), luoghiPossibili,
               Integer.parseInt(oraInizioText.getText()), Integer.parseInt(oraFineText.getText()), datePossibili);

        try {
            //Deserializzo in una lista dinamica
            JsonReader reader = new JsonReader(new FileReader(workspacesFilePath));
            TypeToken<List<Workspace>> token = new TypeToken<List<Workspace>>() {};
            List<Workspace> workspaces = gson.fromJson(reader, token.getType());

            for(Workspace w : workspaces)
                if (w.getID().equals(this.workspace.getID())) {
                    w.aggiungiAttivita(a);
                    this.workspace.aggiungiAttivita(a);
                    break;
                }

            FileWriter writer = new FileWriter(workspacesFilePath);
            gson.toJson(workspaces, writer);
            writer.close();
        } catch (FileNotFoundException e) {
            //Error label
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.aggiornaFileUtenti();

        this.tornaInterfacciaGestioneWorkspace((Stage)createBtn.getScene().getWindow());
    }

    //Aggiorno anche il file utenti
    private void aggiornaFileUtenti(){
        try {
            //Deserializzo in una lista dinamica
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            JsonReader reader = new JsonReader(new FileReader(usersFilePath));
            TypeToken<List<Utente>> token = new TypeToken<List<Utente>>() {};
            List<Utente> utenti = gson.fromJson(reader, token.getType());
            ArrayList<String> workspaces = new ArrayList<String>();

            String[] temp;

            for(Utente u : utenti)
                if (u.getUsername().equals(this.admin.getUsername())) {
                    workspaces.addAll(u.getWorkspacesList());
                    for(String s: workspaces){
                        temp=s.split("x");
                        if(this.workspace.getID().equals(temp[0])){
                            if(temp[1].equals("none")){
                                u.getWorkspacesList().remove(this.workspace.getID()+"x"+"none");
                                u.addToWorkspaceId(this.workspace.getID()+"x"+idTextField.getText());
                            }else{
                                u.addToWorkspaceId(this.workspace.getID()+"x"+idTextField.getText());
                            }
                        }
                    }
                }

            FileWriter writer = new FileWriter(usersFilePath);
            gson.toJson(utenti, writer);
            writer.close();
        } catch (FileNotFoundException e) {
            //Error label
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Converte l'oggetto LocalDate (di DatePicker) in una stringa
    private String convertLocalDateToString() throws NullPointerException{
        ZoneId defaultZoneId = ZoneId.systemDefault();
        LocalDate localDate = datePicker.getValue();
        Date date = Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String strDate = formatter.format(date);
        return strDate;
    }

    private void tornaInterfacciaGestioneWorkspace(Stage stage) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GestioneWorkspace.fxml"));
        Parent root = loader.load();

        GestioneWorkspaceController gestioneWorkspaceController = loader.getController();
        gestioneWorkspaceController.showInfo(workspace, admin, usersFilePath, workspacesFilePath);
        gestioneWorkspaceController.visualizzaAttivita();

        stage.setScene(new Scene(root));

        this.trascinamentoFinestra(root, stage);
    }

    //Verifica se ci sono gia attività con l'ID che sto cercando di inserire
    private boolean verificaIdDoppi(ArrayList<Attivita> attivita){
        for(Attivita a : attivita)
            if(a.getID().equals(idTextField.getText()))
                return true;
        return false;
    }

    @FXML
    void minimizeWindow(MouseEvent event) {
        Stage stage = (Stage) minimizeLabel.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    void undoCreate(MouseEvent event) throws IOException {
        this.tornaInterfacciaGestioneWorkspace((Stage)undoBtn.getScene().getWindow());
    }

    public void showInfo(Utente admin, Workspace w, String usersFile, String workspacesFile){
        this.admin = admin;
        this.workspace = w;
        this.workspaceInfo.setText(w.getDescrizione());
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

    @FXML
    void addData(MouseEvent event) {
        try {
            String newData = this.convertLocalDateToString();
            if (!this.choiceData.getItems().contains(newData)) {
                this.choiceData.getItems().add(newData);
            }
            this.datePicker.setValue(null);
        }catch(NullPointerException exc){}
    }

    @FXML
    void addLuogo(MouseEvent event) {
        String newLuogo = this.luogoText.getText();
        if (!this.choiceLuogo.getItems().contains(newLuogo) && this.luogoText.getText() != "") {
            this.choiceLuogo.getItems().add(newLuogo);
        }
        this.luogoText.setText(null);
    }


}
