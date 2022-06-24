package TeleBot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class TeleBot {

    private final String URL = "https://api.telegram.org/bot5546911577:AAEAcLZRbuzEfKlXCSI0hSQRqozUUo1Rz38";
    public boolean loop = true;
    private Response2 response = new Response2();
    private int lastUpdateID;

    public void start() throws FileNotFoundException {
                String[] message = new String[2];
        while (loop) {
            message = getMessage();
            if (message != null) {
                message = response.getResponse(message);
                sendMessage(message);
            }
            else{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void sendMessage(String message[]) {
        try {
            URL obj = new URL(URL + "/sendMessage?chat_id=" + message[0] + "&text=" + message[1]);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.flush();
            wr.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String output;
            StringBuffer response = new StringBuffer();

            while ((output = in.readLine()) != null) {
                response.append(output);
            }
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] getMessage()  {
        HttpURLConnection con;
        try {
            URL obj = new URL(URL + "/getUpdates");
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");


            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String output;
            StringBuffer response = new StringBuffer();

            while ((output = in.readLine()) != null) {response.append(output);}
            in.close();

            JSONObject jo = new JSONObject(response.toString());
            for(int i = 0; i<jo.names().length(); i++){
                String keyTmp;
                keyTmp = jo.names().getString(i);
                String valueTmp =  jo.get(jo.names().getString(i)).toString();
                if(keyTmp.contains("result")) {

                    JSONArray ja = new JSONArray(valueTmp);
                    int lastHandledUpdate = lastUpdateID;
                    int cnt=-1;
                    int j;
                    for(j = 0; j<ja.length(); j++){
                        jo = ja.getJSONObject(j);
                        cnt = jo.getInt("update_id");
                        if(cnt == lastHandledUpdate){
                            if(j+1== ja.length()) //End Of File
                                return null;
                            else {
                                jo = (JSONObject) ja.get(j+1);
                                lastUpdateID = jo.getInt("update_id");
                                String text;
                                try {
                                    jo = (JSONObject) jo.get("message");
                                    text = (String) jo.get("text");
                                } catch (JSONException e) {
                                    jo = (JSONObject) jo.get("callback_query");
                                    text = jo.getString("data");
                                }
                                jo = (JSONObject) jo.get("from");
                                String idUser = Integer.toString(jo.getInt("id"));
                                String[] values = {idUser, text};
                                return values;
                            }
                        }
                    }
                    jo = ja.getJSONObject(j-1);
                    lastUpdateID = jo.getInt("update_id");

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }



}
