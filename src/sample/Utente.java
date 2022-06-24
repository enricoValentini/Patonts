/*package sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import java.io.*;
import java.util.ArrayList;

public class Utente {
    private String nome;
    private String cognome;
    private String username;
    private String password;
    private boolean admin;
    private ArrayList<String> workspaceId;

    public Utente(String nome,String cognome,String username,String password, boolean admin){
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.password = password;
        this.admin = admin;
        this.workspaceId = new ArrayList<String>();
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    public String getNome(){ return this.nome;}

    public String getCognome(){ return this.cognome;}

    public boolean getAdmin(){ return this.admin;}

    public void addToWorkspaceId(String wrkId){
        this.workspaceId.add(wrkId);
    }

    public void removeId(String wrkId){
        this.workspaceId.remove(wrkId);
    }

    public ArrayList<String> getWorkspacesList(){
        return this.workspaceId;
    }
    public String getWorkspaceId(String element){
        return this.workspaceId.get(this.workspaceId.indexOf(element));
    }

    public String toString() {
        String s = getCognome() + getNome() + getPassword() + getUsername() + getAdmin();
        return s;
    }
}

 */
package sample;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import java.io.*;
import java.util.ArrayList;

public class Utente {
    private String telegramID;
    private String nome;
    private String cognome;
    private String username;
    private String password;
    private boolean admin;
    private ArrayList<String> workspaceId;

    /*
    public Utente(String username, ArrayList<String> workspaceId){
        this.username= username;
        this.workspaceId= workspaceId;
    }
    */
    public Utente(String telegramID,String nome,String cognome,String username,String password, boolean admin){
        this.telegramID = telegramID;
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.password = password;
        this.admin = admin;
        this.workspaceId = new ArrayList<String>();
    }
    public String getID(){
        return this.telegramID;

    }
    public void setID(String id){
        this.telegramID= id;
    }
    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    public String getNome(){ return this.nome;}

    public String getCognome(){ return this.cognome;}

    public boolean getAdmin(){ return this.admin;}

    public void addToWorkspaceId(String wrkId){
        this.workspaceId.add(wrkId);
    }

    public void removeId(String wrkId){
        this.workspaceId.remove(wrkId);
    }

    public ArrayList<String> getWorkspacesList(){
        return this.workspaceId;
    }
    public String getWorkspaceId(String element){
        return this.workspaceId.get(this.workspaceId.indexOf(element));
    }

    public String toString() {
        String s = getCognome() + getNome() + getPassword() + getUsername() + getAdmin();
        return s;
    }
}

