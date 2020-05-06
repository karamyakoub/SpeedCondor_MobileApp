package com.karam.transport;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import androidx.work.ListenableWorker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import static android.content.Context.MODE_PRIVATE;


public class AyncData {
    URL url;
    HttpURLConnection conn;
    Context context;
    DBConnection dbConnection;
    ArrayList<HashMap<String,String>> notasList,pordsList;
    String numcar,numnota,jsonNotas,jsonProds;

    public AyncData(Context context){
        this.context = context;
    }


    public interface OnResult{
        ListenableWorker.Result onFailure(ListenableWorker.Result result);
        ListenableWorker.Result onSuccess(ListenableWorker.Result result);
    }


}
