package com.karam.transport;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.internal.clearcut.zzcn;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Methods {
    public static final String TAG ="METHODS";
    public static boolean isNetworkConnected= true;
    public static AlertDialog loadingDialog;

    //Static method that will set the shared prefrences in this app
    public static void setSharedPref(@NonNull Context contex, @NonNull String type, @NonNull String name, @NonNull Object value){
        SharedPreferences sharedPref = contex.getSharedPreferences("com.example.pendencia",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        switch (type)
        {
            case "string":
                editor.putString(name, (String)value);
                break;
            case "int":
                editor.putInt(name,(int)value);
                break;
            case "float":
                editor.putFloat(name,(float)value);
                break;
            case "long":
                editor.putLong(name,(long)value);
                break;
            case "boolean":
                editor.putBoolean(name,(boolean)value);
                break;
        }
        editor.commit();
    }

    //Static method that gets the shared prefrences in this app
    public  static Object getSharedPref(@NonNull Context context,@NonNull String type,@NonNull String name){
        SharedPreferences sharedPref = context.getSharedPreferences(
                "com.example.pendencia", Context.MODE_PRIVATE);
        Object val = null;
        switch (type)
        {
            case "string":
                val =  (Object) sharedPref.getString(name,"");
                break;
            case "int":
                val =  (Object) sharedPref.getInt(name,0);
                break;
            case "float":
                val =  (Object) sharedPref.getFloat(name,0);
                break;
            case "long":
                val =  (Object) sharedPref.getLong(name,0);
                break;
            case "boolean":
                val =  (Object) sharedPref.getBoolean(name,false);
                break;
        }
        return  val;
    }

    /*Function that will allow to return a HashMap from string that has this structure
    (":NUM_CARGA%:COD_MOTORISTA,"5646464","2131")
     */
    public static HashMap<String, String> stringToHashMap(@NonNull String _params,@NonNull String... _values) {
        HashMap<String, String> hm = new HashMap<>();
        if (_params != null && _values != null && _params.length() > 0) {
            try {
                String[] params = _params.split("%");
                for (int i = 0; i < params.length; i++) {
                    hm.put(params[i], _values[i]);
                }
                return hm;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    //List of hashmap of <string,string> to Json Array that will return String
    public static String toJson(List<HashMap<String, String>> list) {
        JSONArray jarray = new JSONArray();
        for (HashMap item : list) {
            JSONObject jobj = new JSONObject();
            Iterator it = item.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                try {
                    jobj.put(String.valueOf(pairs.getKey()), String.valueOf(pairs.getValue()));
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "";
                }
            }
            jarray.put(jobj);
        }
        return String.valueOf(jarray);
    }


    //Json to List of hashmap that has key,value of readed json
    public static List<HashMap<String, String>> toList(String json) {
        List arr = new ArrayList<HashMap<String, String>>();
        JSONArray jarray = null;
        try {
            jarray = new JSONArray(json);
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jobj = jarray.getJSONObject(i);
                HashMap<String, String> item = new HashMap<String, String>();
                Iterator it = jobj.keys();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    item.put(key, (String) jobj.getString(key));
                }
                arr.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return arr;
    }

    //Json to List of hashmap that has key,value of readed json
    public static HashMap<String, String> toHashMap(String json) {
        JSONObject jobj = null;
        try {
            jobj = new JSONObject(json);
            HashMap<String, String> map = new HashMap<String, String>();
            Iterator it = jobj.keys();
            while (it.hasNext()) {
                String key = (String) it.next();
                map.put(key, (String) jobj.getString(key));
            }
            return map;
        } catch (JSONException e) {
            e.printStackTrace();
            return new HashMap<String,String>();
        }
    }



    //Function to convert json string and sqlStatement to EncodedUrl that can post it to the web server and (it uses toJson function)
    public static String encode(HashMap<String, String> map) throws Exception {
        StringBuilder result = new StringBuilder();
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            if(map.size() != 1){
                Map.Entry pair = (Map.Entry)it.next();
                result.append(URLEncoder.encode(String.valueOf(pair.getKey()), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(String.valueOf(pair.getValue()), "UTF-8"));
                result.append("&");
                it.remove(); // avoids a ConcurrentModificationException
            }else {
                Map.Entry pair = (Map.Entry)it.next();
                //System.out.println(pair.getKey() + " = " + pair.getValue());
                result.append(URLEncoder.encode(String.valueOf(pair.getKey()), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(String.valueOf(pair.getValue()), "UTF-8"));
                it.remove(); // avoids a ConcurrentModificationException
            }
        }
        return String.valueOf(result);
    }

    //function to return an array of the keys of returned array from the sql
    public static List<String> getListHeader(@NonNull List<HashMap<String, String>> list){
        List<String> header=new ArrayList<String>();
        if (list != null && list.size() > 0) {
            Iterator it = ((HashMap<String, String>) list.get(0)).entrySet().iterator();

            while ((it.hasNext())) {
                Map.Entry pairs = (Map.Entry) it.next();
                header.add(String.valueOf(pairs.getKey()));
            }

            return header;
        }
        else
        {
            return null;
        }
    }

    //Check Connection by Mobile or by wifi
    public static void checkConnection(Context context){
        ConnectivityManager connectivityManager =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(Build.VERSION.SDK_INT >= 29){
            try {
                NetworkRequest.Builder builder = new NetworkRequest.Builder();
                connectivityManager.
                        registerNetworkCallback(builder.build(),
                                new ConnectivityManager.
                                        NetworkCallback(){
                                    @Override
                                    public void onAvailable(Network network) {
                                        isNetworkConnected = true; // Global Static Variable
                                    }
                                    @Override
                                    public void onLost(Network network) {
                                        isNetworkConnected = false; // Global Static Variable
                                    }
                                }
                        );
            }catch (Exception e){
                isNetworkConnected = false;
            }
        }else
        {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo !=null){
                isNetworkConnected = networkInfo.isConnected();
            }else{
                isNetworkConnected = false;
            }
        }
    }

    public static void CloseSoftKeyboradOnTouch(Activity activity){
        try{
            InputMethodManager imm =(InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception ex){

        }
    }
    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public static boolean checkValidJson(String str){
        try {
            new JSONObject(str);
        } catch (JSONException e) {
            e.printStackTrace();
            try {
                new JSONArray(str);
            } catch (JSONException ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static View setToastView(Activity activity,String textTitle,Boolean titleVisb,String textMessage,Boolean messageVisb,
                                    String textButtonConfirm,Boolean buttonConfirmVisb,String textButtonDissmiss,Boolean buttonDissmissVisb){
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.costum_toast_dialog,null);
        TextView title = layout.findViewById(R.id.toast_title);
        TextView message = layout.findViewById(R.id.toast_message);
        Button btnConfirm = layout.findViewById(R.id.toast_btn_confirm);
        Button btnDismiss = layout.findViewById(R.id.toast_btn_dismiss);
        title.setText(textTitle);
        title.setVisibility((titleVisb==true)?View.VISIBLE:View.INVISIBLE);
        message.setText(textMessage);
        message.setVisibility((messageVisb==true)?View.VISIBLE:View.INVISIBLE);
        btnConfirm.setText(textButtonConfirm);
        btnConfirm.setVisibility((buttonConfirmVisb==true)?View.VISIBLE:View.INVISIBLE);
        btnDismiss.setText(textButtonDissmiss);
        btnDismiss.setVisibility((buttonDissmissVisb==true)?View.VISIBLE:View.INVISIBLE);

        if(titleVisb == false){
            View vw1 = layout.findViewById(R.id.toast_title);
            ((ViewGroup) vw1.getParent()).removeView(vw1);
        }
        if(messageVisb == false){
            View vw2 = layout.findViewById(R.id.toast_message);
            ((ViewGroup) vw2.getParent()).removeView(vw2);
        }
        if(buttonConfirmVisb == false){
            View vw3 = layout.findViewById(R.id.toast_btn_confirm);
            ((ViewGroup) vw3.getParent()).removeView(vw3);
        }
        if(buttonDissmissVisb == false){
            View vw4 = layout.findViewById(R.id.toast_btn_dismiss);
            ((ViewGroup) vw4.getParent()).removeView(vw4);
        }
        return layout;
    }





    public static String getCurrentDate(){
        Date currentTime = Calendar.getInstance().getTime();
        String strDate = (String) DateFormat.format("dd/MM/yyyy HH:mm:ss",currentTime);
        return strDate;
    }


    public static Long longParser(String input) {
        try {
            Long output = Long.parseLong(input);
            return output;
        } catch (Exception ex) {
            return -1l;
        }
    }

    public static Float FloatParser(String input){
        try{
            Float output = Float.parseFloat(input);
            return output;
        }catch(Exception ex){
            return -1f;
        }
    }

    public static Integer integerParser(String input){
        try{
            Integer output = Integer.parseInt(input);
            return output;
        }catch(Exception ex){
            return -1;
        }
    }

    //Load carga and save it to local database
    public static String loadNewCarga(Context context,String json){
        //Read data from json
        HashMap<String,String> mapCNPD = toHashMap(json);
        if(mapCNPD.containsKey("CARGA")){
            //read and save carga
            List<HashMap<String,String>> cargaMap = toList(mapCNPD.get("CARGA"));
            DBConnection dbConnection = new DBConnection(context);
            Carga carga;
            try{
                carga = new Carga(longParser(cargaMap.get(0).get("NUMCAR")),getCurrentDate(),null);
                dbConnection.insertCarga(carga,"DTFINAL",SQLiteDatabase.CONFLICT_REPLACE);
            }catch (SQLiteException ex){
                return ex.getMessage();
            }
            //clean the memory
            carga =null;
            cargaMap = null;
            //read and save NF
            List<HashMap<String,String>> nfMap = toList((mapCNPD.get("NF")));
            NF nf ;
            try{
                for(HashMap<String,String> map : nfMap){
                    nf=new NF(longParser(map.get("NUMNOTA")),longParser(map.get("NUMCAR")),
                            longParser(map.get("CODCLI")),longParser(map.get("CODUSUR")),
                            map.get("CLIENTE"),map.get("EMAIL_CLIENTE"),map.get("EMAIL_CLIENTE2"),map.get("UF"),map.get("CIDADE"),
                            map.get("BAIRRO"),map.get("OBS1"),map.get("OBS2"),map.get("OBS3"),
                            null,map.get("RCA"),map.get("EMAIL_RCA"),null,null,null,
                            null,null,0,0,0,-1,map.get("ENDERECO"),map.get("CEP"));
                    dbConnection.insertNF(nf,"OBSINTREGA,DTENT,LATENT,LONGTENT,PENDLAT,PENDLONGT",SQLiteDatabase.CONFLICT_REPLACE);
                }
            }catch (SQLiteException ex){
                return ex.getMessage();
            }

            //clean the memory
            nf =null;
            nfMap=null;
            Prod prod;
            List<HashMap<String,String>> prodMap = toList((mapCNPD.get("PROD")));
            try{
                for(HashMap<String,String> map : prodMap){
                    prod = new Prod(longParser(map.get("CODPROD")),longParser(map.get("NUMNOTA")),longParser(map.get("NUMCAR")),longParser(map.get("QT")),
                            0l,longParser(map.get("CODBARRA1")),longParser(map.get("CODBARRA2")),0,0,map.get("DESCRICAO"));

                    dbConnection.insertProd(prod,null,SQLiteDatabase.CONFLICT_IGNORE);
                }
            }catch (SQLiteException ex){
                return ex.getMessage();
            }

            //clear the memory
            prodMap=null;
            prod = null;
            //here should add the part of nota de devolução
            List<HashMap<String,String>> nfMapPend = toList((mapCNPD.get("NFPEND")));
            if(nfMapPend!= null && nfMapPend.size()>0){
                NF nfpend ;
                try{
                    for(HashMap<String,String> map : nfMapPend){
                        nfpend=new NF(longParser(map.get("NUMNOTA")),longParser(map.get("NUMCAR")),
                                longParser(map.get("CODCLI")),longParser(map.get("CODUSUR")),
                                map.get("CLIENTE"),map.get("EMAIL_CLIENTE"),map.get("EMAIL_CLIENTE2"),map.get("UF"),map.get("CIDADE"),
                                map.get("BAIRRO"),map.get("OBS1"),map.get("OBS2"),map.get("OBS3"),
                                null,map.get("RCA"),map.get("EMAIL_RCA"),null,null,null,
                                FloatParser(map.get("LAT")),FloatParser(map.get("LONGT")),0,0,1,-1,map.get("ENDERECO"),map.get("CEP"),
                                longParser(map.get("CODPROCESS")),map.get("DTENTREGA"),map.get("OBSENT"));
                        dbConnection.insertNF(nfpend,"OBSINTREGA,DTENT,LATENT,PENDLONGT",SQLiteDatabase.CONFLICT_REPLACE);
                    }
                }catch (SQLiteException ex){
                    return ex.getMessage();
                }

                //clean the memory
                nf =null;
                nfMap=null;
                Prod prodPend;
                List<HashMap<String,String>> prodMapPend = toList((mapCNPD.get("PRODPEND")));
                try{
                    for(HashMap<String,String> map : prodMapPend){
                        prodPend = new Prod(longParser(map.get("CODPROD")),longParser(map.get("NUMNOTA")),longParser(map.get("NUMCAR")),longParser(map.get("QT")),
                                0l,longParser(map.get("CODBARRA1")),longParser(map.get("CODBARRA2")),0,0,map.get("DESCRICAO")
                                ,longParser(map.get("QTFALTA")),integerParser(map.get("CODMOTIVO")));
                        dbConnection.insertProd(prodPend,null,SQLiteDatabase.CONFLICT_IGNORE);
                    }
                }catch (SQLiteException ex){
                    return ex.getMessage();
                }
            }

            //check the maximum number of saved carga and delete the older if the number is 5
            Cursor c = null;
            long numcar = dbConnection.deleteAbove5();
            deleteCargaAudios(context,numcar);
            return "ok";
        }else{
            return mapCNPD.get("status") + " : "+ mapCNPD.get("cod")+"\n"+mapCNPD.get("message");
        }
    }

    public static void setWidthHeight(View view,int multiplayer,@NonNull  boolean type){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(view.getWidth()*multiplayer,view.getHeight()*multiplayer);;
        if(type == false) {
            params = new LinearLayout.LayoutParams(view.getWidth() / multiplayer, view.getHeight() / multiplayer);
        }
        params.gravity= Gravity.CENTER;
        view.setLayoutParams(params);
    }

    public static String getBase64FromPath(String path) {
        String base64 = "";
            File file = new File(path);
            if(file.exists()){
                try {
                    byte[] buffer = new byte[(int) file.length() + 100];
                    @SuppressWarnings("resource")
                    int length = new FileInputStream(file).read(buffer);
                    base64 = Base64.encodeToString(buffer, 0, length,
                            Base64.DEFAULT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        return base64;
    }

    public static String getStatus(NF nf,Activity activity){
        switch (nf.getStent()){
            case 1:
                return activity.getString(R.string.notas_status_Entregue);
            case 3:
                return activity.getString(R.string.notas_status_Entregue_pend);
            case 4:
                return activity.getString(R.string.notas_status_Entregue_dev);
            default:
                return activity.getString(R.string.notas_status_emtransito);
        }
    }

    public static float roundFloat(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }


    public static boolean checkPlayServices(Context context,Activity activity) {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(context);
        if(result == ConnectionResult.SUCCESS) {
            return true;
        }else if (result == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED){
            googleAPI.showErrorDialogFragment(activity,result,0);
            return false;
        }else{
            return false;
        }
    }

    public static void deleteCargaAudios(Context context,long numcar){
        File mediaStorageDir = context.getDir("records",Context.MODE_PRIVATE);
        File[] files = mediaStorageDir.listFiles();
        if(files.length>0){
            for (File file:files){
                if(file.getName().split("-")[0].trim().equals(String.valueOf(numcar).trim())){
                    try{
                        file.delete();
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public static void showLoadingDialog(Context context){
        loadingDialog = new AlertDialog.Builder(context)
                .setView(R.layout.load_layout)
                .setCancelable(false)
                .create();
        loadingDialog.show();
    }

    public static void closeLoadingDialog(){
        loadingDialog.dismiss();
    }



    public static boolean checkGPSTurndOnWithOutAct(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {

        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {

        }

        if (!gps_enabled && !network_enabled) {
            return false;
        } else {
            return true;
        }
    }






    public static boolean checkGPSTurndOn(Context context, final Activity activity) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            View view = Methods.setToastView(activity, "", false, context.getString(R.string.gps_notEnables)
                    , true, "Configurações", true, "Fechar", true);
            final AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setView(view).create();
            Button btnConfirm = view.findViewById(R.id.toast_btn_confirm);
            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    activity.startActivity(intent);
                }
            });
            Button btnCancel = view.findViewById(R.id.toast_btn_dismiss);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.show();
            return false;
        } else {
            return true;
        }
    }

    public static void showEmailInvalidMsg(Activity activity){
        View view = Methods.setToastView(activity,"",false,activity.getString(R.string.invalid_email_format),
                true,"",false,"",false);
        Toast toast = Toast.makeText(activity, "", Toast.LENGTH_LONG);
        toast.setView(view);
        toast.show();
    }

    public  static void showCostumeToast(Activity activity,String msg){
        View view = Methods.setToastView(activity, "", false, msg,
                true, "", false, "", false);
        Toast toast = Toast.makeText(activity, "", Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }
}
