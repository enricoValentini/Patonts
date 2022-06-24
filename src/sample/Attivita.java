package sample;

import java.util.ArrayList;

public class Attivita {
    private String ID;
    private String descrizione;
    private String[] luogo;
    private int oraInizio;
    private int oraFine;
    private String[] dataAttivita;
    private ArrayList<Preferenza> preferenze;

    public Attivita(String ID, String descrizione, String[] luogo, int oraFine, int oraInizio, String[] dataAttivita, ArrayList<Preferenza> preferenze){
        this.ID = ID;
        this.descrizione = descrizione;
        this.luogo = luogo;
        this.oraFine = oraFine;
        this.oraInizio = oraInizio;
        this.dataAttivita = dataAttivita;
        this.preferenze = preferenze;

    }
    public Attivita(String ID, String descrizione, String[] luogo, int oraInizio, int oraFine, String[] dataAttivita){
        this.ID = ID;
        this.descrizione = descrizione;
        this.luogo = luogo;
        this.oraFine = oraFine;
        this.oraInizio = oraInizio;
        this.dataAttivita = dataAttivita;
        this.preferenze = new ArrayList<Preferenza>();
    }
    //DA ELIMINARE
   /* public Attivita(String ID, String descrizione,  int oraFine, int oraInizio){
        this.ID = ID;
        this.descrizione = descrizione;
       //DA ELIMINARE SOLO PROVAS
        this.oraFine = oraFine;
        this.oraInizio = oraInizio;
       ;
        this.preferenze = new ArrayList<Preferenza>();
    }*/
    //

    public Attivita(String descrizione, int oraInizio, int oraFine){
        this.descrizione = descrizione;
        this.oraFine = oraFine;
        this.oraInizio = oraInizio;
    }

    public ArrayList<Preferenza> getPreferenzeList(){
        return this.preferenze;
    }

    public void setPreferenze(ArrayList<Preferenza> pref){
        this.preferenze = pref;
    }

    public void setDescrizione(String descrizione){
        this.descrizione = descrizione;
    }

    public void setLuogo(String[] luogo){
        this.luogo = luogo;
    }

    public void setOraInizio(int oraInizio){
        this.oraInizio = oraInizio;
    }

    public void setOraFine(int oraFine){
        this.oraFine = oraFine;
    }

    public String[] getDataAttivita(){ return this.dataAttivita;}

    public void setDataAttivita(String[] dataAttivita){
        this.dataAttivita = dataAttivita;
    }

    public String getID(){
        return this.ID;
    }

    public String getDescrizione(){
        return this.descrizione;
    }

    public String[] getLuogo(){
        return this.luogo;
    }

    public int getOraInizio(){
        return this.oraInizio;
    }

    public int getOraFine(){
        return this.oraFine;
    }


    public String toString(){
        return this.ID +" "+ this.descrizione +" "+ this.luogo +" "+ this.oraFine +" "+ this.oraInizio +" "+ this.dataAttivita +" "+ this.preferenze;
    }



}
