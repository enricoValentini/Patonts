package sample;

import java.util.ArrayList;

public class Amministratore extends Utente{

    public Amministratore(String  telegramID, String nome,String cognome,String username,String password, boolean admin){
        super( telegramID, nome,cognome,username, password, admin);
    }

    public String toString(){
        String s = getCognome() + getNome() + getPassword() + getUsername() + getAdmin();
        return s;
    }
}
