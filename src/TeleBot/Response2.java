package TeleBot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import sample.Attivita;
import sample.Preferenza;
import sample.Utente;
import sample.Workspace;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Response2 {

    private Utente user;
    private static String usersFilePath = "users.json";
    private static String WorkspaceFilePath = "workspaces.json";
    private String HELP = "Mi puoi controllare usando i seguenti comandi: %0A /getActivity - Ti informo in quali attivity sei iscritto e le preferenze che hai scelto.%0A /newPreference puoi modificare una preferenza %0A /deletePreference puoi cancellare una preferenza";
    ArrayList<String> logged = new ArrayList<String>();

    public static void changeUserPath(String n){
        usersFilePath = n;
    }
    public static void changeWksPath(String n){
        WorkspaceFilePath = n;
    }


    protected String[] getResponse(String[] message) {


        if (!logged.contains(message[0])) {
            LoginOK(message);

        }
        else if(message[1].contains("*del*")){
            eliminaPref(user.getUsername(),message[1].substring(0,message[1].indexOf("*")));
            message[1]="Preferenza rimossa con successo!";
        }
        else if(message[1].contains("*luogo*")){
            String[] s;
            s = getLuoghi(user.getUsername(),message[1].substring(0,message[1].indexOf("*")));
            String markup= "Scegli un luogo&reply_markup={#inline_keyboard# : [";
            for(int i=0;i<s.length-1;i++){

                markup+="[{#text#:#"+s[i]+ "#,#callback_data#:#";
                markup+= s[i]+";"+s[s.length-1]+"*data*" +"# }],";

            }

            markup=markup.substring(0,markup.length()-1) + "]}";
            message[1]=stringPretty(markup);
        }
        else if(message[1].contains("*data*")){
            message[1]=message[1].substring(0,message[1].indexOf("*"));
            String[] s= message[1].split(";");
            setLuogo(user.getUsername(),s[1],s[0]);
            String[] d= getDate(user.getUsername(),s[1]);

            String markup= "Scegli una data&reply_markup={#inline_keyboard# : [";
            for(int i=0;i<d.length-1;i++){

                markup+="[{#text#:#"+d[i]+ "#,#callback_data#:#";
                markup+= d[i]+";"+d[d.length-1]+"*setPref*" +"# }],";

            }

            markup=markup.substring(0,markup.length()-1) + "]}";
            message[1]=stringPretty(markup);


        }
        else if(message[1].contains("*setPref*")){
            message[1]=message[1].substring(0,message[1].indexOf("*"));
            String[] d= message[1].split(";");
            setData(user.getUsername(),d[1],d[0]);
            message[1]="Preferenza modificata con successo!";
        }
        else{
                String markup;
                ArrayList<Preferenza> out = new ArrayList<>();
            switch (message[1]) {
                case "/getActivity" -> {
                    out = getAttivita(user.getUsername());
                    message[1] = "";
                    for (Preferenza p : out) {
                        message[1] += stringPretty(p.toString());
                        ;
                    }
                }
                case "/deletePreference" -> {
                    markup = "Quale preferenza vuoi rimuovere?&reply_markup={#inline_keyboard# : [";
                    out = getAttivita(user.getUsername());
                    for (Preferenza p : out) {
                        if (!p.getDatePref().equals(" non selezionata")) {
                            markup += "[{#text#:#" + p.getDescrizione().toString() + "#,#callback_data#:#";
                            markup += p.getDescrizione() + "*del*" + "# }],";
                        }
                    }
                    if (markup.equals("Quale preferenza vuoi rimuovere?&reply_markup={#inline_keyboard# : [")) {
                        message[1] = "Nessuna preferenza da rimuovere!";
                        break;
                    }
                    markup = markup.substring(0, markup.length() - 1) + "]}";
                    message[1] = stringPretty(markup);
                }
                case "/newPreference" -> {
                    markup = "Quale preferenza vuoi modificare?&reply_markup={#inline_keyboard# : [";
                    out = getAttivita(user.getUsername());
                    for (Preferenza p : out) {
                        markup += "[{#text#:#" + p.getDescrizione().toString() + " Luogo: " + p.getLuogoPref().toString() + " Data: " + p.getDatePref().toString() + "#,#callback_data#:#";
                        markup += p.getDescrizione() + "*luogo*" + "# }],";
                    }
                    markup = markup.substring(0, markup.length() - 1) + "]}";
                    message[1] = stringPretty(markup);
                }
                default -> message[1] = HELP;
            }
        }
        return message;
    }

    protected void LoginOK(String[] message) { // completo

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            JsonReader reader = new JsonReader(new FileReader(usersFilePath)); //PASSO IL JSON DI UTENTI
            TypeToken<List<Utente>> token = new TypeToken<List<Utente>>() {
            };
            List<Utente> utenti = gson.fromJson(reader, token.getType());

            String[] credentials = new String[2];
            credentials = message[1].split(";"); //DAL MESSEGGIO RICAVO USERNAME E PASSWORD SEPARATI DA ;
            int tmp = credentials.length;
            for (Utente u : utenti) {
                if (tmp < 2) { //L'UTENTE HA SBAGLIATO LA "STRUTTURA" DEL MESSAGGIO
                    message[1] = "Per utlizzare il bot inserisci username e password con la seguente sintassi:%0A'username';'password'";
                    // return null;
                } else if (credentials[0].equals(u.getUsername()) && credentials[1].equals(u.getPassword()) && !u.getAdmin()) {
                    logged.add(message[0]); //AGIGUNGO L'UTENTE AL ARRAY DEI LOGGED
                    user= u;
                    message[1] = "Accesso riuscito! " + HELP;

                    if (u.getID() != message[0]) { //SE L'UTENE FA L/ACCESSO PER LA PRIMA VOLTA AGGIUNGO IL CAMPO TELEGRAM ID
                        u.setID(message[0]);
                        File jfile = new File(usersFilePath);
                        OutputStream out = new FileOutputStream(jfile);
                        out.write(gson.toJson(utenti).getBytes());
                        out.flush();
                    }

                    break;
                } else {  //USRN O PASS SBAGLIATI
                    message[1] = "Username o password errati, riprovare";
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("file non trovato");
            message[1]="Importare file Utenti e Worksapce";
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("inserisci username e password con la seguente sintassi: 'username';'password'");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected ArrayList<Preferenza> getAttivita(String userN) {

        ArrayList<Preferenza> preference = new ArrayList<Preferenza>(); //INIZIALIZZO ARRAY DI PREFERENZE VUOTO
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            JsonReader reader = new JsonReader(new FileReader(usersFilePath)); //PASSO IL FILE DI UTENTI
            TypeToken<List<Utente>> tokenA = new TypeToken<List<Utente>>() {};

            List<Utente> utenti = gson.fromJson(reader, tokenA.getType());


            for(Utente u: utenti) {  //CICLO SU TUTI GLI UTENTI E CERCO LA CORRISPONDENZA DI ID
                if (u.getUsername() != null) {
                    if (u.getUsername().equals(userN)) {
                        ArrayList<String> tmp = u.getWorkspacesList(); //MI SALVO I WORKSAPCE E LE ATTIVITA DELL'UTENTE
                        for (String s : tmp) {
                            String[] wa = s.split("x");//SEPARO ID WORKSPACE DA ID ATTIVITA

                            reader = new JsonReader(new FileReader(WorkspaceFilePath)); //PASSO IL FILE WORSPACE
                            List<Workspace> workspaces = new Gson().fromJson(reader, new TypeToken<List<Workspace>>(){}.getType());
                            for (Workspace w : workspaces) {

                                if (w.getID().equals(wa[0])) { // CERCO LA CORRISPONDEZA ID WORSPACE

                                    List<Attivita> attivita = w.getListAttivita(); //MI SALVO LE ATTIVITA PRESENTI NEL WORSPACE
                                    for (Attivita a : attivita) { //CERCO LA CORRISPONDENZA DI ID ATTIVITA
                                        if (a.getID().equals(wa[1])) {
                                            ArrayList<Preferenza> pref = a.getPreferenzeList(); //MI SALVO LE PREFERENZE
                                            System.out.println(pref);

                                            if (pref.size()==0){  //PREFERENCE E' VUOTO
                                                Preferenza prf = new Preferenza(a.getDescrizione(), " non selezionato", a.getOraInizio(), a.getOraFine(), " non selezionata");
                                                preference.add(prf);
                                                break;
                                            } else {  //PREFRENCE CONTIENE ELEMENTI E C'E' UNA CORRISPONDENZA DI ID
                                                boolean flag=false;
                                                for (Preferenza p : pref) {
                                                    if (p.getUserName().equals(userN)) {
                                                        preference.add(p);
                                                        flag = true;
                                                        break;
                                                    }
                                                }
                                                    if(flag==false){ //PREFRENZE CONTIENE ELEMENTI MA NON C'E' CORRISPONDENZA TRA ID
                                                        Preferenza prf = new Preferenza(a.getDescrizione(), " non selezionato", a.getOraInizio(), a.getOraFine(), " non selezionata");
                                                        preference.add(prf);
                                                        break;
                                                    }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            reader.close();
            return preference;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void eliminaPref(String userN, String descrizione){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            JsonReader reader = new JsonReader(new FileReader(usersFilePath)); //PASSO IL FILE DI UTENTI
            TypeToken<List<Utente>> tokenA = new TypeToken<List<Utente>>() {};

            List<Utente> utenti = gson.fromJson(reader, tokenA.getType());


            for(Utente u: utenti) {  //CICLO SU TUTI GLI UTENTI E CERCO LA CORRISPONDENZA DI ID
                if (u.getUsername() != null) {
                    if (u.getUsername().equals(userN)) {
                        ArrayList<String> tmp = u.getWorkspacesList(); //MI SALVO I WORKSAPCE E LE ATTIVITA DELL'UTENTE
                        for (String s : tmp) {
                            String[] wa = s.split("x");//SEPARO ID WORKSPACE DA ID ATTIVITA

                            reader = new JsonReader(new FileReader(WorkspaceFilePath)); //PASSO IL FILE WORSPACE
                            List<Workspace> workspaces = new Gson().fromJson(reader, new TypeToken<List<Workspace>>(){}.getType());
                            for (Workspace w : workspaces) {

                                if (w.getID().equals(wa[0])) { // CERCO LA CORRISPONDEZA ID WORSPACE

                                    List<Attivita> attivita = w.getListAttivita(); //MI SALVO LE ATTIVITA PRESENTI NEL WORSPACE
                                    if(attivita.size()<1)
                                        break;

                                    for (Attivita a : attivita) { //CERCO LA CORRISPONDENZA TRA ID ATTIVITA
                                        if (a.getID().equals(wa[1])) {
                                            ArrayList<Preferenza> pref = a.getPreferenzeList(); //MI SALVO LE PREFERENZE
                                            for(Preferenza p: pref){
                                                if(p.getUserName().equals(userN)&&p.getDescrizione().equals(descrizione)){ //CERCO LA CORRISPONDENZA TRA ID E DESCRIZIONE
                                                    p.setDataAttivitaPref(" non selezionata");
                                                    p.setLuogoPref(" non selezionato");
                                                    File jfile = new File(WorkspaceFilePath);
                                                    OutputStream out = new FileOutputStream(jfile);
                                                    out.write(gson.toJson(workspaces).getBytes());
                                                    out.flush();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected String[] getLuoghi(String userN, String descrizione){
        String[] luoghi;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            JsonReader reader = new JsonReader(new FileReader(usersFilePath)); //PASSO IL FILE DI UTENTI
            TypeToken<List<Utente>> tokenA = new TypeToken<List<Utente>>() {};

            List<Utente> utenti = gson.fromJson(reader, tokenA.getType());


            for(Utente u: utenti) {  //CICLO SU TUTI GLI UTENTI E CERCO LA CORRISPONDENZA DI ID
                if (u.getUsername() != null) {
                    if (u.getUsername().equals(userN)) {
                        ArrayList<String> tmp = u.getWorkspacesList(); //MI SALVO I WORKSAPCE E LE ATTIVITA DELL'UTENTE
                        for (String s : tmp) {
                            String[] wa = s.split("x");//SEPARO ID WORKSPACE DA ID ATTIVITA

                            reader = new JsonReader(new FileReader(WorkspaceFilePath)); //PASSO IL FILE WORSPACE
                            List<Workspace> workspaces = new Gson().fromJson(reader, new TypeToken<List<Workspace>>(){}.getType());
                            for (Workspace w : workspaces) {

                                if (w.getID().equals(wa[0])) { // CERCO LA CORRISPONDEZA ID WORSPACE

                                    List<Attivita> attivita = w.getListAttivita(); //MI SALVO LE ATTIVITA PRESENTI NEL WORSPACE
                                    for (Attivita a : attivita) { //CERCO LA CORRISPONDENZA DI ID ATTIVITA
                                        if (a.getID().equals(wa[1])&&a.getDescrizione().equals(descrizione)) {
                                            luoghi = a.getLuogo();
                                            luoghi = Arrays.copyOf(luoghi, luoghi.length + 1);
                                            luoghi[luoghi.length-1]=a.getDescrizione().toString();
                                            return luoghi;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;


    }

    protected void setLuogo(String userN, String descrizione, String newLuogo){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            JsonReader reader = new JsonReader(new FileReader(usersFilePath)); //PASSO IL FILE DI UTENTI
            TypeToken<List<Utente>> tokenA = new TypeToken<List<Utente>>() {};

            List<Utente> utenti = gson.fromJson(reader, tokenA.getType());


            for(Utente u: utenti) {  //CICLO SU TUTI GLI UTENTI E CERCO LA CORRISPONDENZA DI ID
                if (u.getID() != null) {
                    if (u.getUsername().equals(userN)) {
                        ArrayList<String> tmp = u.getWorkspacesList(); //MI SALVO I WORKSAPCE E LE ATTIVITA DELL'UTENTE
                        for (String s : tmp) {
                            String[] wa = s.split("x");//SEPARO ID WORKSPACE DA ID ATTIVITA

                            reader = new JsonReader(new FileReader(WorkspaceFilePath)); //PASSO IL FILE WORSPACE
                            List<Workspace> workspaces = new Gson().fromJson(reader, new TypeToken<List<Workspace>>(){}.getType());
                            for (Workspace w : workspaces) {

                                if (w.getID().equals(wa[0])) { // CERCO LA CORRISPONDEZA ID WORSPACE
                                    List<Attivita> attivita = w.getListAttivita(); //MI SALVO LE ATTIVITA PRESENTI NEL WORSPACE

                                    for (Attivita a : attivita) { //CERCO LA CORRISPONDENZA DI ID ATTIVITA
                                        if (a.getID().equals(wa[1])&&a.getDescrizione().equals(descrizione)) {
                                            ArrayList<Preferenza> pref = a.getPreferenzeList(); //MI SALVO LE PREFERENZE

                                                if (pref.size()==0){  //PREFERENCE E' VUOTO

                                                    Preferenza prf = new Preferenza(userN,a.getDescrizione(), newLuogo, a.getOraInizio(), a.getOraFine(), " non selezionata");
                                                    pref.add(prf);
                                                    a.setPreferenze(pref);
                                                    Writer writer = Files.newBufferedWriter(Paths.get(WorkspaceFilePath));
                                                    gson.toJson(workspaces, writer);
                                                    writer.close();
                                                    return;
                                                } else {  //PREFRENCE CONTIENE ELEMENTI E C'E' UNA CORRISPONDENZA DI ID
                                                    boolean flag=false;
                                                    for (Preferenza p : pref) {
                                                        if (p.getUserName().equals(userN)) {
                                                            p.setLuogoPref(newLuogo);
                                                            File jfile = new File(WorkspaceFilePath);
                                                            OutputStream out = new FileOutputStream(jfile);
                                                            out.write(gson.toJson(workspaces).getBytes());
                                                            out.flush();
                                                            flag = true;
                                                            return;
                                                        }
                                                    }
                                                    if(flag==false){ //PREFRENZE CONTIENE ELEMENTI MA NON C'E' CORRISPONDENZA TRA ID
                                                        Preferenza prf = new Preferenza(userN,a.getDescrizione(), newLuogo, a.getOraInizio(), a.getOraFine(), " non selezionata");
                                                        pref.add(prf);
                                                        a.setPreferenze(pref);
                                                        Writer writer = Files.newBufferedWriter(Paths.get(WorkspaceFilePath));
                                                        gson.toJson(workspaces, writer);
                                                        writer.close();
                                                        return;
                                                    }
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected String[] getDate(String userN, String descrizione){

        String[] date;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            JsonReader reader = new JsonReader(new FileReader(usersFilePath)); //PASSO IL FILE DI UTENTI
            TypeToken<List<Utente>> tokenA = new TypeToken<List<Utente>>() {};

            List<Utente> utenti = gson.fromJson(reader, tokenA.getType());


            for(Utente u: utenti) {  //CICLO SU TUTI GLI UTENTI E CERCO LA CORRISPONDENZA DI ID
                if (u.getID() != null) {
                    if (u.getUsername().equals(userN)) {
                        ArrayList<String> tmp = u.getWorkspacesList(); //MI SALVO I WORKSAPCE E LE ATTIVITA DELL'UTENTE
                        for (String s : tmp) {
                            String[] wa = s.split("x");//SEPARO ID WORKSPACE DA ID ATTIVITA

                            reader = new JsonReader(new FileReader(WorkspaceFilePath)); //PASSO IL FILE WORSPACE
                            List<Workspace> workspaces = new Gson().fromJson(reader, new TypeToken<List<Workspace>>(){}.getType());
                            for (Workspace w : workspaces) {

                                if (w.getID().equals(wa[0])) { // CERCO LA CORRISPONDEZA ID WORSPACE

                                    List<Attivita> attivita = w.getListAttivita(); //MI SALVO LE ATTIVITA PRESENTI NEL WORSPACE
                                    for (Attivita a : attivita) { //CERCO LA CORRISPONDENZA DI ID ATTIVITA
                                        if (a.getID().equals(wa[1])&&a.getDescrizione().equals(descrizione)) {
                                            date = a.getDataAttivita();
                                            date = Arrays.copyOf(date, date.length + 1);
                                            date[date.length-1]=a.getDescrizione().toString();
                                            return date;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;


    }

    protected void setData(String userN, String descrizione, String newData){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            JsonReader reader = new JsonReader(new FileReader(usersFilePath)); //PASSO IL FILE DI UTENTI
            TypeToken<List<Utente>> tokenA = new TypeToken<List<Utente>>() {};

            List<Utente> utenti = gson.fromJson(reader, tokenA.getType());


            for(Utente u: utenti) {  //CICLO SU TUTI GLI UTENTI E CERCO LA CORRISPONDENZA DI ID
                if (u.getUsername() != null) {
                    if (u.getUsername().equals(userN)) {
                        ArrayList<String> tmp = u.getWorkspacesList(); //MI SALVO I WORKSAPCE E LE ATTIVITA DELL'UTENTE
                        for (String s : tmp) {
                            String[] wa = s.split("x");//SEPARO ID WORKSPACE DA ID ATTIVITA

                            reader = new JsonReader(new FileReader(WorkspaceFilePath)); //PASSO IL FILE WORSPACE
                            List<Workspace> workspaces = new Gson().fromJson(reader, new TypeToken<List<Workspace>>(){}.getType());
                            for (Workspace w : workspaces) {

                                if (w.getID().equals(wa[0])) { // CERCO LA CORRISPONDEZA ID WORSPACE
                                    List<Attivita> attivita = w.getListAttivita(); //MI SALVO LE ATTIVITA PRESENTI NEL WORSPACE

                                    for (Attivita a : attivita) { //CERCO LA CORRISPONDENZA DI ID ATTIVITA
                                        if (a.getID().equals(wa[1])&&a.getDescrizione().equals(descrizione)) {
                                            ArrayList<Preferenza> pref = a.getPreferenzeList(); //MI SALVO LE PREFERENZE
                                                for (Preferenza p : pref) {
                                                    if (p.getUserName().equals(userN)) {
                                                        p.setDataAttivitaPref(newData);
                                                        File jfile = new File(WorkspaceFilePath);
                                                        OutputStream out = new FileOutputStream(jfile);
                                                        out.write(gson.toJson(workspaces).getBytes());
                                                        out.flush();
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }





    private String stringPretty(String str) {
        str = str.replace('#', '"');
        str = str.replace("$", "%0A");
        str = str.replace("!", "%0A%0A%0A");
        return str;
    }


}


