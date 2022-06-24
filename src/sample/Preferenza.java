package sample;

public class Preferenza extends Attivita {

    private String userName;
    private String luogoPref;
    private String dataAttivitaPref;

    public Preferenza(String userName, String descrizione,
                      String luogoPref, int oraInizio, int oraFine, String dataAttivitaPref){
        super(descrizione,oraInizio,oraFine);

        this.userName = userName;
        this.luogoPref = luogoPref;
        this.dataAttivitaPref = dataAttivitaPref;
    }



    public Preferenza(String descrizione, String luogoPref, int oraInizio, int oraFine, String dataAttivitaPref){
        super(descrizione,oraInizio,oraFine);
        this.luogoPref = luogoPref;
        this.dataAttivitaPref = dataAttivitaPref;
    }
    @Override
    public void setDescrizione(String descrizione){
        super.setDescrizione(descrizione);
    }
    @Override
    public void setOraInizio(int oraInizio){
        super.setOraInizio(oraInizio);
    }
    @Override
    public void setOraFine(int oraFine){
        super.setOraFine(oraFine);
    }
    @Override
    public String getDescrizione(){
        return super.getDescrizione();
    }
    @Override
    public int getOraInizio(){
        return super.getOraInizio();
    }
    @Override
    public int getOraFine(){
        return super.getOraFine();
    }
    public void setLuogoPref(String luogo){
        this.luogoPref = luogo;
    }
    public String getLuogoPref(){
        return this.luogoPref;
    }
    public String getDatePref(){ return this.dataAttivitaPref; }
    public void setDataAttivitaPref(String dataAttivitaPref){
        this.dataAttivitaPref = dataAttivitaPref;
    }

    public void setUserName(String userName){
        this.userName=userName;
    }
    public String getUserName(){
        return this.userName;
    }

    public String toString(){
        return super.getDescrizione()+"$Ora inizio: "+super.getOraInizio()+"$Ora fine: "+super.getOraFine() +"$Luogo: "+  this.luogoPref  +"$Data: "+ this.dataAttivitaPref+"!";//+ this.userName;
    }


}
