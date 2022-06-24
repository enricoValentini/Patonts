package sample;

import java.util.ArrayList;

public class UtenteSemplice extends Utente{
    private ArrayList<Attivita> attivita;   //Attivita a cui l utente partecipa

    public UtenteSemplice(String telegramID, String nome, String cognome, String username, String password, boolean admin) {
        super(telegramID, nome, cognome, username, password, admin);
    }
}
