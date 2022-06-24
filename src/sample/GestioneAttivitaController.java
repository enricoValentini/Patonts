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
import javafx.scene.control.*;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GestioneAttivitaController {

    @FXML
    private Label closeLabel;

    @FXML
    private Label minimizeLabel;

    @FXML
    private Label activityInfo;

    @FXML
    private Label workspaceInfo;

    @FXML
    private TextArea descrizioneTextArea;

    @FXML
    private TextField luogoText;

    @FXML
    private TextField oraInizioText;

    @FXML
    private TextField oraFineText;

    @FXML
    private Button modifyBtn;

    @FXML
    private Button goBackBtn;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Label errorLabel;

    @FXML
    private Button addDataBtn;

    @FXML
    private Button addLuogoBtn;

    @FXML
    private ChoiceBox<String> choiceData;

    @FXML
    private ChoiceBox<String> listaLuoghi;

    @FXML
    private Label noUsersLabel;

    @FXML
    private VBox usersList;

    private Utente admin;
    private Workspace workspace;
    private Attivita attivita;
    private double xOffset = 0;
    private double yOffset = 0;
    private String usersFilePath = "users.json";
    private String workspacesFilePath = "workspaces.json";
    private String[] datePossibili;
    private String[] luoghiPossibili;

    public void showInfo(Utente admin, Workspace w, Attivita attivita, String usersFile, String workspacesFile){
        this.admin = admin;
        this.workspace = w;
        this.attivita = attivita;
        this.activityInfo.setText("Attività" + " - " +this.attivita.getDescrizione());
        this.workspaceInfo.setText(this.workspace.getDescrizione());
        this.descrizioneTextArea.setText(this.attivita.getDescrizione());
        this.oraInizioText.setText(String.valueOf(this.attivita.getOraInizio()));
        this.oraFineText.setText(String.valueOf(this.attivita.getOraFine()));
        this.usersFilePath = usersFile;
        this.workspacesFilePath = workspacesFile;
        this.listaLuoghi.getItems().addAll(attivita.getLuogo());
        this.choiceData.getItems().addAll(attivita.getDataAttivita());
        this.luoghiPossibili = new String[this.listaLuoghi.getItems().size()];
        this.luoghiPossibili = attivita.getLuogo();
        this.datePossibili = new String[this.choiceData.getItems().size()];
        this.datePossibili = attivita.getDataAttivita();
    }

    private LocalDate convertStringToLocalDate(String activityDate){
        try {
            Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(activityDate);
            return date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    @FXML
    void closeWindow(MouseEvent event) {
        Stage stage = (Stage) closeLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    //Torno al pannello del workspace
    void goBack(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GestioneWorkspace.fxml"));
        Parent root = loader.load();

        GestioneWorkspaceController gestioneWorkspaceController = loader.getController();
        gestioneWorkspaceController.showInfo(workspace, admin, usersFilePath, workspacesFilePath);
        gestioneWorkspaceController.visualizzaAttivita();

        Stage stage = (Stage)goBackBtn.getScene().getWindow();
        stage.setScene(new Scene(root));

        this.trascinamentoFinestra(root, stage);
    }

    @FXML
    void minimizeWindow(MouseEvent event) {
        Stage stage = (Stage) minimizeLabel.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    //Modifico l'attività
    void modifyActivity(MouseEvent event) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //Controllo se i campi sono stati riempiti
        if(descrizioneTextArea.getText() == "" || oraFineText.getText() == "" ||
                oraInizioText.getText() == ""){
            this.errorLabel.setText("I campi non possono essere vuoti!");
            this.errorLabel.setTextFill(Color.RED);
            this.errorLabel.setVisible(true);
            return;
        }

        //Controllo che i valori delle ore siano compresi tra 0 e 23
        try {
            if (Integer.parseInt(oraInizioText.getText()) <= 0 || Integer.parseInt(oraInizioText.getText()) >= 23 ||
                    Integer.parseInt(oraFineText.getText()) <= 0 || Integer.parseInt(oraFineText.getText()) >= 23){
                oraInizioText.setText(null);
                oraFineText.setText(null);
                this.errorLabel.setText("Formato ore non valido. Riprova!");
                this.errorLabel.setTextFill(Color.RED);
                this.errorLabel.setVisible(true);
                return;
            }
        }catch(NumberFormatException exc){
            oraInizioText.setText(null);
            oraFineText.setText(null);
            this.errorLabel.setText("Formato ore non valido. Riprova!");
            this.errorLabel.setTextFill(Color.RED);
            this.errorLabel.setVisible(true);
            return;
        }
        try {
            JsonReader reader = new JsonReader(new FileReader(workspacesFilePath));
            TypeToken<List<Workspace>> token = new TypeToken<List<Workspace>>() {};
            List<Workspace> workspaces = gson.fromJson(reader, token.getType());

            //Serve a modificare il contenuto del file
            //Ma modificheremo anche gli oggetti che stiamo utilizzando ora
            //quelli di attività e workspace
            for(Workspace w : workspaces)
                if(w.getID().equals(this.workspace.getID())) {
                    for(Attivita a : w.getListAttivita())
                        if(a.getID().equals(this.attivita.getID())) {

                            //Aggiorna gli elementi dei 2 array
                            this.aggiornaLuoghiEDate();

                            //Rimuovo gli elementi dei 2 ChoiceBox
                            this.rimuovieElementiChoiceBox();

                            a.setDescrizione(this.descrizioneTextArea.getText());
                            a.setLuogo(this.luoghiPossibili);
                            a.setOraInizio(Integer.parseInt(this.oraInizioText.getText()));
                            a.setOraFine(Integer.parseInt(this.oraFineText.getText()));
                            a.setDataAttivita(this.datePossibili);

                            showInfo(admin, w, a, usersFilePath, workspacesFilePath);
                            break;
                        }
                    break;
                }

            try {
                FileWriter writer = new FileWriter(workspacesFilePath);
                gson.toJson(workspaces, writer);
                writer.close();
            } catch (JsonIOException e) {
                System.out.println("JsonException");
            } catch (IOException e) {
                System.out.println("IOException");
            }

            errorLabel.setText("Attività modificata con successo!");
            errorLabel.setTextFill(Color.GREEN);
            errorLabel.setVisible(true);

        }catch(FileNotFoundException exc){
            errorLabel.setText("File non trovato!");
            errorLabel.setTextFill(Color.RED);
            errorLabel.setVisible(true);
        }
    }

    private void aggiornaLuoghiEDate(){
        luoghiPossibili = new String[listaLuoghi.getItems().size()];
        for(int i=0; i < luoghiPossibili.length; i++) {
            listaLuoghi.getSelectionModel().select(i);
            this.luoghiPossibili[i] = listaLuoghi.getValue();
        }

        datePossibili = new String[choiceData.getItems().size()];
        for(int i=0; i < datePossibili.length; i++) {
            choiceData.getSelectionModel().select(i);
            this.datePossibili[i] = choiceData.getValue();
        }
    }

    private void rimuovieElementiChoiceBox(){
        //Rimuovo gli elementi nei choicebox
        int size = this.listaLuoghi.getItems().size();
        this.listaLuoghi.getItems().remove(0,size);

        size = this.choiceData.getItems().size();
        this.choiceData.getItems().remove(0,size);
    }

    public void caricaUsers(){
        ArrayList<Preferenza> preferenze = new ArrayList<Preferenza>();
        preferenze.addAll(attivita.getPreferenzeList());

        if(preferenze.size() == 0)
            this.noUsersLabel.setVisible(true);
        else
            this.addUsersToVBox(preferenze);
    }

    private void addUsersToVBox(ArrayList<Preferenza> preferenze){
        for(Preferenza p : preferenze)
            usersList.getChildren().add(createGraphicPreference(p));
    }

    //Creo graficamente la preferenza
    private StackPane createGraphicPreference(Preferenza p){
        StackPane preferencePane = new StackPane();
        preferencePane.setPrefWidth(345);
        preferencePane.setPrefHeight(40);
        preferencePane.setStyle("-fx-background-color:#21476e; -fx-border-color:#ffffff");
        preferencePane.setAlignment(Pos.CENTER);

        preferencePane.setOnMouseEntered(new EventHandler() {
            @Override
            public void handle(Event event) {
                preferencePane.setCursor(Cursor.HAND);
            }
        });

        preferencePane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("MostraPreferenza.fxml"));
                try {
                    //Apro una finestra per visualizzare le preferenze dell' utente
                    Parent root = loader.load();
                    MostraPreferenzeController mostraPreferenzeController = loader.getController();
                    mostraPreferenzeController.showInfo(admin, workspace, attivita, usersFilePath, workspacesFilePath, p);

                    Stage stage = (Stage) goBackBtn.getScene().getWindow();
                    stage.setScene(new Scene(root));

                    trascinamentoFinestra(root, stage);
                }catch(IOException exc){}
            }
        });


        Label title = new Label(p.getUserName());
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setAlignment(Pos.CENTER);
        title.setTextFill(Color.WHITE);
        preferencePane.getChildren().add(title);
        return preferencePane;

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
        if (!this.listaLuoghi.getItems().contains(newLuogo) && this.luogoText.getText() != "") {
            this.listaLuoghi.getItems().add(newLuogo);
        }
        this.luogoText.setText(null);
    }

    @FXML
    void openAddUserWindow(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("CreazioneUtente.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage)goBackBtn.getScene().getWindow();
        CreaUtenteController creaUtenteController = loader.getController();
        creaUtenteController.showInfo(admin, workspace, attivita, usersFilePath, workspacesFilePath);
        stage.setScene(new Scene(root));

        this.trascinamentoFinestra(root, stage);
    }
}
