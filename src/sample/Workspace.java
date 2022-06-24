package sample;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

//Insieme di attività
//Può essere creato e gestito solo da un utente AMMINISTRATORE
public class Workspace {
    private String ID;
    private String descrizione;
    private ArrayList<Attivita> attivita;  //Insieme di attivita del workspace
    //private ArrayList<UtenteSemplice> utenti;

    //Costruttore
    public Workspace(String ID, String descrizione){
        this.ID = ID;
        this.descrizione = descrizione;
        this.attivita = new ArrayList<Attivita>();
        //this.utenti = new ArrayList<UtenteSemplice>();
    }

    public void setDescrizione(String descrizione){
        this.descrizione = descrizione;
    }
    public String getID(){
        return this.ID;
    }

    public String getDescrizione(){
        return this.descrizione;
    }

    public ArrayList<Attivita> getListAttivita(){
        return this.attivita;
    }

    public void aggiungiAttivita(Attivita a){
        this.attivita.add(a);
    }

    //public ArrayList<UtenteSemplice> getListUtenti(){
       // return this.utenti;
    //}
}
