package com.karam.transport;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginCarregFrag extends Fragment implements TaskListener, View.OnTouchListener , View.OnClickListener {
    String TAG;
    EditText carregEditText;
    Button carregButton;
    TextView responseTxtVw;
    ProgressBar carregProgress;
    LinearLayout carregLayoutFrag;
    String numCarga;
    boolean isPaused=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TAG = getString(R.string.login_carreg_frag);
        View view = inflater.inflate(R.layout.fragment_login_carreg, container, false);
        carregEditText = view.findViewById(R.id.carreg_etxt);
        carregButton = view.findViewById(R.id.carreg_btn);
        responseTxtVw = view.findViewById(R.id.carreg_response_txtv);
        carregProgress = view.findViewById(R.id.carreg_progress);
        carregLayoutFrag = view.findViewById(R.id.login_carreg_frag);
        carregLayoutFrag.setOnTouchListener(this);
        carregButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        isPaused = false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.login_carreg_frag){
            Methods.CloseSoftKeyboradOnTouch(getActivity());
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        Methods.checkConnection(getContext());
        if (v.getId() == R.id.carreg_btn)
        {
            if(Methods.isNetworkConnected) {
                responseTxtVw.setVisibility(View.VISIBLE);
                numCarga = String.valueOf(carregEditText.getText());
                if (!numCarga.matches("")) {
                    //configure the UI elementes
                    carregProgress.setVisibility(View.VISIBLE);
                    carregButton.setEnabled(false);
                    carregEditText.setEnabled(false);
                    try {
                        long codmotorista = (long) Methods.getSharedPref(getActivity(), "long", getString(R.string.SHcodmotorista));
                        HashMap<String, String> map = Methods.stringToHashMap("NUMCAR%CODMOTORISTA", numCarga, String.valueOf(codmotorista));
                        String encodedParams = Methods.encode(map);
                        SRVConnection connection = new SRVConnection(null, this, "response");
                        connection.execute(getString(R.string.url_server_host) + getString(R.string.url_server_load_carreg), encodedParams);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    responseTxtVw.setText(getString(R.string.carreg_missing_info));
                }
            }
        }else{
            responseTxtVw.setText(getString(R.string.internet_connectivity_error));
        }
    }


    @Override
    public void onTaskFinish(String response) {
        if(Methods.checkValidJson(response)){//check if the response is json or jarray or just a string
            responseTxtVw.setText("Carregando");
            new AsyncTask<String, String, String>() {
                @Override
                protected String doInBackground(String... strings) {
                    String res =(Methods.loadNewCarga(getActivity(),strings[0]));
                    return res;
                }

                @Override
                protected void onPostExecute(String res) {
                    if(res=="ok"){
                        try{
                            Methods.setSharedPref(getContext(),"long",getString(R.string.SHcarga),Methods.longParser(numCarga));
                            Intent intent = new Intent(getActivity(),NotasActivity.class);
                            if(isPaused==false){
                                startActivity(intent);
                            }
                            getActivity().finish();
                            //add work to check notas sending and location tracking
                            try{
                                final Constraints constraints = new Constraints.Builder().setRequiredNetworkType(
                                        NetworkType.CONNECTED).build();
                                final PeriodicWorkRequest workRequest = new  PeriodicWorkRequest.Builder(
                                        DataWorker.class,15, TimeUnit.MINUTES)
                                        .setConstraints(constraints)
                                        .build();
                                WorkManager.getInstance(getContext()).enqueueUniquePeriodicWork(getString(R.string.tag_unique_work), ExistingPeriodicWorkPolicy.REPLACE,workRequest);
                            }catch (Exception ex){
                            }
                        }catch (Exception ex){
                        }
                    }else{
                        responseTxtVw.setText(res);
                    }
                    //configure the UI elementes
                    carregButton.setEnabled(true);
                    carregEditText.setEnabled(true);
                    carregProgress.setVisibility(View.INVISIBLE);
                }
            }.execute(response);
        }else{
            responseTxtVw.setText((response!=null) ? response : getString(R.string.login_invalid_json_error));
            //configure the UI elementes
            carregButton.setEnabled(true);
            carregEditText.setEnabled(true);
            carregProgress.setVisibility(View.INVISIBLE);
        }
    }
}