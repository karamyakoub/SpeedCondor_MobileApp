package com.karam.transport;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.google.common.util.concurrent.ListenableFuture;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.HttpsURLConnection;

import static android.content.Context.MODE_PRIVATE;

public class DataWorker extends ListenableWorker {
    URL url;
    HttpURLConnection conn;
    DBConnection dbConnection;
    ArrayList<HashMap<String,String>> notasList,pordsList;
    String numcar,numnota,jsonNotas,jsonProds;
    final static String TAG ="DATAWORKER";

    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public DataWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        if(Methods.checkGPSTurndOnWithOutAct(getApplicationContext())){
            SingleShotLocationProvider.requestSingleUpdate(getApplicationContext(), new SingleShotLocationProvider.LocationCallback() {
                @Override
                public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                    enviar(location.latitude,location.longitude);
                    Log.i(TAG, "Work done with location");
                }
            });
        }else{
            enviar(0,0);
            Log.i(TAG, "Work done without location");
        }

        return new ListenableFuture<Result>() {
            @Override
            public void addListener(Runnable listener, Executor executor) {
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public Result get() throws ExecutionException, InterruptedException {
                return null;
            }

            @Override
            public Result get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
                return null;
            }
        };
    }
    public void enviar(final double lat, final double longt){
        new AsyncTask<Void,Void,String>(){
            @Override
            protected String doInBackground(Void... voids) {
                HashMap<String, String> map = new HashMap<>();
                dbConnection = new DBConnection(getApplicationContext());
                String returnVal="";
                notasList = new ArrayList<>();
                pordsList = new ArrayList<>();
                Cursor c = dbConnection.select(false, "NF", null,
                        " STENVI = 0 AND STENT>0 AND NUMCAR=?",
                        new String[]{String.valueOf(numcar)}
                        , null, null, "CLIENTE", null);
                if(c.getCount()>0) {
                    c.moveToFirst();
                    HashMap<String, String> mapNF;
                    while (c != null && !c.isAfterLast()) {
                        mapNF = new HashMap<>();
                        numnota = String.valueOf(c.getLong(c.getColumnIndex("NUMNOTA")));
                        File mediaStorageDir = getApplicationContext().getDir("records", MODE_PRIVATE);
                        if (!mediaStorageDir.exists()) {
                            mediaStorageDir.mkdirs();
                        }
                        String outputFile = mediaStorageDir.getAbsolutePath() + File.separator + numcar + "-" + numnota + ".3gp";
                        String fileName = numcar + "-" + numnota + ".3gp";
                        String audio = Methods.getBase64FromPath(outputFile);
                        mapNF.put(":NUMNOTA", numnota);
                        mapNF.put(":NUMCAR", numcar);
                        mapNF.put(":LAT", String.valueOf(c.getFloat(c.getColumnIndex("LATENT"))));
                        mapNF.put(":LONGT", String.valueOf(c.getFloat(c.getColumnIndex("LONGTENT"))));
                        mapNF.put(":DTENTREGA", String.valueOf(c.getString(c.getColumnIndex("DTENT"))));
                        mapNF.put(":OBSENT", String.valueOf(c.getString(c.getColumnIndex("OBSENTREGA"))));
                        mapNF.put(":EMAIL_CLIENTE", String.valueOf(c.getString(c.getColumnIndex("EMAIL_CLIENTE2"))));
                        mapNF.put(":STATUS", String.valueOf(c.getInt(c.getColumnIndex("STENT"))));
                        mapNF.put(":STCRED", String.valueOf(c.getInt(c.getColumnIndex("STCRED"))));
                        mapNF.put(":CODMOTIVO", String.valueOf(c.getInt(c.getColumnIndex("CODMOTIVO"))));
                        mapNF.put(":AUDIO", audio);
                        mapNF.put(":FILENAME", fileName);
                        notasList.add(mapNF);

                        if (Methods.integerParser(mapNF.get(":STATUS")) == 2) {
                            Cursor c2 = dbConnection.select(false, "PROD", null,
                                    "NUMCAR =? AND NUMNOTA=? AND STDEV>0", new String[]{numcar, numnota}
                                    , null, null, "DESCRICAO", null);
                            if (c2.getCount() > 0) {
                                c2.moveToFirst();
                                HashMap<String, String> mapProd;
                                while (c2 != null && !c2.isAfterLast()) {
                                    mapProd = new HashMap<>();
                                    mapProd.put(":CODPROD", String.valueOf(c2.getLong(c2.getColumnIndex("CODPROD"))));
                                    mapProd.put(":QT", String.valueOf(c2.getLong(c2.getColumnIndex("QTFALTA"))));
                                    mapProd.put(":CODMOTIVO", String.valueOf(c2.getInt(c2.getColumnIndex("CODMOTIVO"))));
                                    mapProd.put(":STDEV", String.valueOf(c2.getInt(c2.getColumnIndex("STDEV"))));
                                    mapProd.put(":NUMCAR", numcar);
                                    mapProd.put(":NUMNOTA", numnota);
                                    pordsList.add(mapProd);
                                    c2.moveToNext();
                                }
                                c2.close();
                            }
                        }
                        c.moveToNext();
                    }
                    c.close();
                    jsonNotas = Methods.toJson(notasList);
                    jsonProds = Methods.toJson(pordsList);
                }
                map = Methods.stringToHashMap("NOTAJSON%PRODJSON%NUMCAR%LAT%LONGT", jsonNotas, jsonProds, numcar,
                        String.valueOf(lat), String.valueOf(longt));
                String encodedParams = null;
                try {
                    encodedParams = Methods.encode(map);
                    setConn(getApplicationContext().getString(R.string.url_server_host) + getApplicationContext().getString(R.string.url_server_save_auto));
                    response(encodedParams);
                    return "ok";
                } catch (Exception e) {
                    e.printStackTrace();
                    return "not ok";
                }
            }

            @Override
            protected void onPostExecute(String s) {
                if(s!=null && s.trim().equals("ok")){
                    dbConnection = new DBConnection(getApplicationContext());
                    NF nf = new NF();
                    nf.setStenvi(1);
                    dbConnection.updatetNF(nf,"NUMCAR=?",new String[]{String.valueOf(numcar).trim()});
                }
            }
        }.execute();
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

    private String response(String params)
    {
        StringBuffer sb = new StringBuffer("");
        try
        {
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(params);
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
                return String.valueOf(sb);
            }
            else
            {
                return conn.getResponseMessage();
            }
        }catch(Exception e)
        {
            return e.getMessage();
        }
    }
}