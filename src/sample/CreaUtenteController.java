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
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreaUtenteController {

    @FXML
    private Button goBackBtn;

    @FXML
    private Button creaBtn;

    @FXML
    private Label closeLabel;

    @FXML
    private Label minimizeLabel;

    @FXML
    private Label nomeAttivita;

    @FXML
    private Label activityInfo;

    @FXML
    private TextField cognomeText;

    @FXML
    private TextField nomeText;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameText;

    @FXML
    private ChoiceBox<String> choiceUtente;

    @FXML
    private Label errorLabel;

    @FXML
    private Button addExistingUserBtn;


    private double xOffset = 0;
    private double yOffset = 0;
    private String usersFilePath = "users.json";
    private String workspacesFilePath = "workspaces.json";
    private Utente admin;
    private Workspace workspace;
    private Attivita attivita;

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

    public void showInfo(Utente admin, Workspace w, Attivita attivita, String usersFile, String workspacesFile){
        this.activityInfo.setText("Aggiungi utente");
        this.workspacesFilePath = workspacesFile;
        this.usersFilePath = usersFile;
        this.nomeAttivita.setText(attivita.getDescrizione());
        this.workspace = w;
        this.attivita = attivita;
        this.admin = admin;
        this.caricaUtentiDaSistema();
    }

    //Riempie il choiceBox per selezionare gli utenti
    private void caricaUtentiDaSistema(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //Leggo dal file utenti
        try{
            JsonReader reader = new JsonReader(new FileReader(usersFilePath));
            TypeToken<List<Utente>> token = new TypeToken<List<Utente>>() {};
            List<Utente> users = gson.fromJson(reader, token.getType());

            for(Utente u : users)
                if(!u.getAdmin())
                    this.choiceUtente.getItems().add(u.getUsername());

            reader.close();

        }catch(JsonIOException exc){

        }catch(IOException exc){

        }
    }

    //Crea un nuovo utente
    @FXML
    void creaUtente(MouseEvent event) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String nome = this.nomeText.getText();
        String cognome = this.cognomeText.getText();
        String username = this.usernameText.getText();
        String password = this.passwordField.getText();

        //Verifico che non ci sia la presenza di un username uguale a quello nuovo
        try {
            JsonReader reader = new JsonReader(new FileReader(usersFilePath));
            TypeToken<List<Utente>> token = new TypeToken<List<Utente>>() {};
            List<Utente> users = gson.fromJson(reader, token.getType());

            //Verifico che i campi non siano vuoti
            if(this.nomeText.getText() == "" || this.cognomeText.getText() == "" ||
                this.usernameText.getText() == "" || this.passwordField.getText() == ""){

                this.errorLabel.setText("I campi non possono essere vuoti");
                this.errorLabel.setTextFill(Color.RED);
                this.errorLabel.setVisible(true);

                return;
            }

            //Verifico che non ci sia la presenza di un username uguale a quello nuovo
            for(Utente u : users)
                if(u.getUsername().equals(username)) {
                    this.errorLabel.setText("Username identico nel sistema. Riprova!");
                    this.errorLabel.setVisible(true);
                    this.usernameText.setText(null);
                    return;
                }

            this.nomeText.setText(null);
            this.cognomeText.setText(null);
            this.usernameText.setText(null);
            this.passwordField.setText(null);

            Utente newUser = new Utente(null, nome, cognome,
                    username, password, false);

            newUser.addToWorkspaceId(this.workspace.getID() + "x" + this.attivita.getID());

            users.add(newUser);

            this.errorLabel.setText("Utente inserito con successo!");
            this.errorLabel.setTextFill(Color.GREEN);
            this.errorLabel.setVisible(true);

            reader.close();

            //Aggiorno il file utenti
            try {
                FileWriter writer = new FileWriter(usersFilePath);
                gson.toJson(users, writer);
                writer.close();
            } catch (JsonIOException e) {
                System.out.println("JsonException");
            } catch (IOException e) {
                System.out.println("IOException");
            }


        }catch(JsonIOException exc){

        }catch(IOException exc){}

        //Creo quindi una nuova preferenza
        Preferenza newPref = new Preferenza(username, attivita.getDescrizione(),
                "Non selezionato", attivita.getOraInizio(), attivita.getOraFine(),
                "Non selezionata");

        this.attivita.getPreferenzeList().add(newPref);

        //Aggiorna file workspace
        this.aggiornaWorkspace(newPref);

        //Reimposta choiceBox
        this.rimuoviElementiChoiceBox();

        //Richiamo showInfo per aggiornare tutte le informazioni
        this.showInfo(admin, workspace, attivita, usersFilePath, workspacesFilePath);

    }

    //Reimposta choiceBox Utenti
    private void rimuoviElementiChoiceBox(){
        //Rimuovo gli elementi nei choicebox
        int size = this.choiceUtente.getItems().size();
        this.choiceUtente.getItems().remove(0,size);
    }

    //Per aggiornare anche il file di workspace
    //e anche l' oggetto workspace
    private void aggiornaWorkspace(Preferenza newPref){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            JsonReader reader = new JsonReader(new FileReader(workspacesFilePath));
            TypeToken<List<Workspace>> token = new TypeToken<List<Workspace>>() {
            };
            List<Workspace> workspaces = gson.fromJson(reader, token.getType());
            ArrayList<Preferenza> pref = new ArrayList<Preferenza>();

            //Aggiorno il file
            for (Workspace w : workspaces) {
                if (w.getID().equals(workspace.getID())) {
                    List<Attivita> att = w.getListAttivita();
                    for (Attivita a : att) {
                        if (a.getID().equals(attivita.getID()) && w.getID().equals(workspace.getID())) {
                            pref = a.getPreferenzeList();
                            pref.add(newPref);
                            a.setPreferenze(pref);
                        }
                    }
                }
            }
            reader.close();

            //Sovraschivo il file di workspace
            try {
                FileWriter writer = new FileWriter(workspacesFilePath);
                gson.toJson(workspaces, writer);
                writer.close();
            } catch (JsonIOException e) {
                System.out.println("JsonException");
            } catch (IOException e) {
                System.out.println("IOException");
            }

        }catch(JsonIOException exc){

        }catch(IOException exc){}
    }

    @FXML
    void addExistingUser(MouseEvent event) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            JsonReader reader = new JsonReader(new FileReader(usersFilePath));
            TypeToken<List<Utente>> token = new TypeToken<List<Utente>>() {
            };
            List<Utente> users = gson.fromJson(reader, token.getType());
            String workspaceId = this.workspace.getID() + "x" + this.attivita.getID();
            for(Utente u : users)
                if(u.getUsername().equals(choiceUtente.getValue()))
                    if(u.getWorkspacesList().contains(workspaceId)) {
                        this.errorLabel.setText("Utente già iscritto! Riprova!");
                        this.errorLabel.setTextFill(Color.RED);
                        this.errorLabel.setVisible(true);
                        return;
                    }else{
                        u.addToWorkspaceId(workspaceId);

                        //Creo quindi una nuova preferenza
                        Preferenza newPref = new Preferenza(choiceUtente.getValue(), attivita.getDescrizione(),
                                "Non selezionato", attivita.getOraInizio(), attivita.getOraFine(),
                                "Non selezionata");

                        this.attivita.getPreferenzeList().add(newPref);
                        //Aggiorno anche il file workspaces
                        this.aggiornaWorkspace(newPref);

                        this.errorLabel.setText("Utente iscritto corretamente!");
                        this.errorLabel.setTextFill(Color.GREEN);
                        this.errorLabel.setVisible(true);

                        this.rimuoviElementiChoiceBox();

                        //Richiamo showInfo per aggiornare tutte le informazioni
                        this.showInfo(admin, workspace, attivita, usersFilePath, workspacesFilePath);
                    }
            //Sovrascrivo il file users
            try {
                FileWriter writer = new FileWriter(usersFilePath);
                gson.toJson(users, writer);
                writer.close();
            } catch (JsonIOException e) {
                System.out.println("JsonException");
            } catch (IOException e) {
                System.out.println("IOException");
            }


        }catch(FileNotFoundException exc){

        }catch(IOException exc){}
    }

    //Torna alla schermata di gestione ativita
    @FXML
    void goBack(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GestioneAttivita.fxml"));
        Parent root = loader.load();

        GestioneAttivitaController gestioneAttivitaController = loader.getController();

        gestioneAttivitaController.showInfo(admin, workspace, attivita, usersFilePath, workspacesFilePath);
        gestioneAttivitaController.caricaUsers();

        Stage stage = (Stage)goBackBtn.getScene().getWindow();
        stage.setScene(new Scene(root));

        this.trascinamentoFinestra(root, stage);
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
