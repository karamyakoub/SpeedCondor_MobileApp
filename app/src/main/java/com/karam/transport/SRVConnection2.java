package com.karam.transport;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class SRVConnection2 extends AsyncTaskLoader {
    private String functionality;
    String link,input;
    private URL url;
    private HttpURLConnection conn;
    final String TAG = "SRVCONNECTION";

    //functionality (request , response , reqres)
    public SRVConnection2(@NonNull Context context,String functionality,String link,String input) {
        super(context);
        this.functionality = functionality;
        this.input = input;
        this.link = link;
    }

    @Nullable
    @Override
    public Object loadInBackground() {
        URL url = null;
        setConn(link);
        switch (functionality)
        {
            case "request":
                return request(input);
            case "response":
                return response(input);
            default:
                return null;
        }
    }


    private String setConn(String link){
        try
        {
            url = new URL(link);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(20000);
            conn.setConnectTimeout(20000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            return "ok";
        }catch (Exception e)
        {
            return e.getMessage();
        }
    }

    private String request(String input){
        try
        {
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(input);
            writer.flush();
            writer.close();
            os.close();
            return conn.getResponseMessage();
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
    }

    private String response(String input) {
        StringBuffer sb = new StringBuffer("");
        try {
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(input);
            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode(); // To Check for 200
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = "";
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    break;
                }
                in.close();
                return sb.toString();
            } else {
                return conn.getResponseMessage();
            }
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
