package sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditAttivitaUtente{

    @FXML
    private Button editDatePlace;

    @FXML
    private Button goBackBtn;

    @FXML
    private Label nomeECognome;

    @FXML
    private Label closeLabel;

    @FXML
    private Label minimizeLabel;

    @FXML
    private Label descAttivita;

    @FXML
    private Label oraInizio;

    @FXML
    private Label oraFine;

    @FXML
    private Label luogoPref;

    @FXML
    private Label dataPref;

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

    private String Nwks;
    private Utente user;
    private String workspace;
    private String attivita;
    private double xOffset = 0;
    private double yOffset = 0;
    private String usersFilePath = "users.json";
    private String workspacesFilePath = "workspaces.json";

    public void showInfo(String workspace, String attivita, Utente user, String usersFile, String workspacesFile, String Nwks){
        this.workspace = workspace;
        this.attivita = attivita;
        this.user = user;
        this.nomeECognome.setText(this.user.getNome() + " " + this.user.getCognome());
        this.usersFilePath = usersFile;
        this.workspacesFilePath = workspacesFile;
        this.Nwks=Nwks;
    }

    //riempio i label con le info salvate nel gson
    protected void getAttivita(String workspace, String attivita){

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            JsonReader reader = new JsonReader(new FileReader(workspacesFilePath)); //PASSO IL FILE WORKSPACE
            List<Workspace> workspaces = new Gson().fromJson(reader, new TypeToken<List<Workspace>>(){}.getType());

            for (Workspace w : workspaces) {

                if (w.getID().equals(workspace)) { // CERCO LA CORRISPONDEZA ID WORSPACE
                    List<Attivita> att = w.getListAttivita(); //MI SALVO LE ATTIVITA PRESENTI NEL WORSPACE
                    for (Attivita a : att) { //CERCO LA CORRISPONDENZA DI ID ATTIVITA
                        if (a.getID().equals(attivita)&&w.getID().equals(workspace)) {

                            descAttivita.setText(a.getDescrizione());

                            oraFine.setText("Ora Fine: " + a.getOraFine());
                            oraInizio.setText("Ora Inizio: "+ a.getOraInizio());

                            ArrayList<Preferenza> prefs= a.getPreferenzeList();
                            boolean flag = false;
                            for(Preferenza p : prefs){
                                if(user.getUsername().equals(p.getUserName())){
                                    flag=true;
                                    luogoPref.setText("Luogo: " + p.getLuogoPref());
                                    dataPref.setText("Data: " + p.getDatePref());
                                    return;
                                }
                            }
                            if(!flag){
                                luogoPref.setText("Luogo: Non selezionato");
                                dataPref.setText("Data: Non selezionata");
                            }


                        }
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    } //funziona{

    @FXML
    //faccio apparire una finestra pop-up
    protected void HandleButtonAction(ActionEvent event) throws IOException {

       Stage stage;
       Parent root;
       stage = new Stage();
       FXMLLoader loader = new FXMLLoader(getClass().getResource("EditDateTime.fxml"));
       root = loader.load();
       EditDateTime editDateTime = loader.getController();
       editDateTime.showInfo(workspace,attivita,user,usersFilePath,workspacesFilePath);
       editDateTime.fillDate();
       stage.setScene(new Scene(root));
       stage.initModality(Modality.APPLICATION_MODAL);
       editDateTime.annullaBtn.setOnAction(actionEvent -> stage.close());
       editDateTime.SalvaBtn.setOnAction(actionEvent -> {
           try {
               if(editDateTime.dataMenu.getValue()!=null && editDateTime.luogoMenu.getValue()!=null) { //se l'utente ha riempito correttamente tutti i campi
                   editDateTime.salvaModifica();                                                       //le modifiche verranno essere salvate altrimenti verra stampato un messaggio di errore
                   getAttivita(workspace,attivita);
                   stage.close();
               }
               else{
                   editDateTime.errorLabel.setText("Tutti i campi devono essere riempiti!");
                   editDateTime.errorLabel.setVisible(true);
               }

           } catch (IOException e) {
               e.printStackTrace();
           }
       });
        stage.showAndWait(); //devo fare una selezione per chiudere il pop up
    }


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


        FXMLLoader loader = new FXMLLoader(getClass().getResource("PannelloAttivitaUtente.fxml"));
        Parent root = loader.load();
        PannelloAttivitaUtente attivitaUtente = loader.getController();

        ArrayList<String> tmp = user.getWorkspacesList();
        ArrayList<String> attivita = new ArrayList<>();
        String[] split;
        for(String s: tmp){
            split = s.split("x");
            if(split[0].equals(workspace)){
                attivita.add(split[1]);
            }
        }
        attivitaUtente.setNwks(Nwks);
        attivitaUtente.showInfo(workspace, attivita, user, usersFilePath, workspacesFilePath, Nwks);
        attivitaUtente.caricaAttivita();
        Stage stage = (Stage)goBackBtn.getScene().getWindow();
        stage.setScene(new Scene(root));
        this.trascinamentoFinestra(root, stage);
    }
}

