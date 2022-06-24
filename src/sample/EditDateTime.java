package sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EditDateTime{

    @FXML
    private Label closeLabel;

    @FXML
    private Label minimizeLabel;

    @FXML
    protected Label errorLabel;

    @FXML
    protected Button SalvaBtn;

    @FXML
    protected Button annullaBtn;

    @FXML
    protected Label descWrks;

    @FXML
    protected ChoiceBox<String> dataMenu;

    @FXML
    protected ChoiceBox<String> luogoMenu;

    private double xOffset = 0;
    private double yOffset = 0;
    private Utente user;
    private String workspace;
    private String attivita;
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

    public void showInfo(String workspace, String attivita, Utente user, String usersFile, String workspacesFile){
        this.workspace = workspace;
        this.attivita = attivita;
        this.user = user;
        this.usersFilePath = usersFile;
        this.workspacesFilePath = workspacesFile;
    }

    //riempio il menu a tendina con gli elementi presenti su json
    protected void fillDate(){
        try {
            JsonReader reader = new JsonReader(new FileReader(workspacesFilePath)); //PASSO IL FILE WORKSPACE
            List<Workspace> workspaces = new Gson().fromJson(reader, new TypeToken<List<Workspace>>(){}.getType());
            System.out.println("debug filldate 1");
            for (Workspace w : workspaces) {
                System.out.println(w);
                System.out.println(workspace+"  sto cercando");
                if (w.getID().equals(workspace)) { // CERCO LA CORRISPONDEZA ID WORSPACE
                    System.out.println("debug filldate 1.5");
                    List<Attivita> att = w.getListAttivita(); //MI SALVO LE ATTIVITA PRESENTI NEL WORSPACE
                    System.out.println("debug filldate 2");
                    for (Attivita a : att) { //CERCO LA CORRISPONDENZA DI ID ATTIVITA
                        if (a.getID().equals(attivita)&&w.getID().equals(workspace)) {

                            System.out.println("debug filldate 3");
                            this.luogoMenu.getItems().addAll(a.getLuogo());
                            this.dataMenu.getItems().addAll(a.getDataAttivita());
                            this.descWrks.setText(a.getDescrizione());
                            return;
                        }
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    protected void salvaModifica() throws IOException {

        if(dataMenu.getValue()==null || luogoMenu.getValue()==null) {

            errorLabel.setText("Tutti i campi devono essere riempiti!");
            errorLabel.setVisible(true);
        }else{
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonReader reader = new JsonReader(new FileReader(workspacesFilePath)); //PASSO IL FILE WORKSPACE
            List<Workspace> workspaces = new Gson().fromJson(reader, new TypeToken<List<Workspace>>(){}.getType());
            for (Workspace w : workspaces) {
                if (w.getID().equals(workspace)) { // CERCO LA CORRISPONDEZA ID WORSPACE
                    List<Attivita> att = w.getListAttivita(); //MI SALVO LE ATTIVITA PRESENTI NEL WORSPACE
                    for (Attivita a : att) { //CERCO LA CORRISPONDENZA DI ID ATTIVITA
                        if (a.getID().equals(attivita)&&w.getID().equals(workspace)) {
                            ArrayList<Preferenza> pref = a.getPreferenzeList(); //MI SALVO LE PREFERENZE
                            if (pref.size()==0){  //PREFERENCE E' VUOTO

                                Preferenza prf = new Preferenza(user.getUsername(),a.getDescrizione(), dataMenu.getValue() , a.getOraInizio(), a.getOraFine(), luogoMenu.getValue());
                                pref.add(prf);
                                a.setPreferenze(pref);
                                Writer writer = Files.newBufferedWriter(Paths.get(workspacesFilePath));
                                gson.toJson(workspaces, writer);
                                writer.close();
                                return;
                            } else {  //PREFRENCE CONTIENE ELEMENTI E C'E' UNA CORRISPONDENZA DI ID
                                boolean flag=false;
                                for (Preferenza p : pref) {
                                    if (p.getUserName().equals(user.getUsername())) {
                                        p.setLuogoPref(luogoMenu.getValue());
                                        p.setDataAttivitaPref(dataMenu.getValue());
                                        File jfile = new File(workspacesFilePath);
                                        OutputStream out = new FileOutputStream(jfile);
                                        out.write(gson.toJson(workspaces).getBytes());
                                        out.flush();
                                        flag = true;
                                        break;
                                    }
                                }
                                if(flag==false){ //PREFRENZE CONTIENE ELEMENTI MA NON C'E' CORRISPONDENZA TRA ID
                                    Preferenza prf = new Preferenza(user.getUsername(),a.getDescrizione(), luogoMenu.getValue(), a.getOraInizio(), a.getOraFine(), dataMenu.getValue());
                                    pref.add(prf);
                                    a.setPreferenze(pref);
                                    Writer writer = Files.newBufferedWriter(Paths.get(workspacesFilePath));
                                    gson.toJson(workspaces, writer);
                                    writer.close();
                                    return;
                                }

                            }
                        }
                    }
                }
            }
            reader.close();
        }


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







}
