package com.karam.transport;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.HashMap;

public class CheckActivity extends AppCompatActivity implements View.OnClickListener , View.OnTouchListener ,TaskListenerAct{
    LinearLayout masterLinLayaout;
    Button startButton,devButton,rentregaButton,perButton;
    TextView codcli_check_txtvw,cliente_check_txtvw,nf_check_txtvw;
    String outputFile,email_cliente,email_cliente2,obs;
    boolean isDoubleBackClick = false;
    int devRentregaPer=-1;
    DBConnection dbConnection;
    Bundle args;
    NF nf;
    AlertDialog alertDialog;
    int position,posSpinner;
    long numcar,numnota,codcli;
    String cliente;


    public void setPosSpinner(int posSpinner) {
        this.posSpinner = posSpinner;
    }

    public void setEmail_cliente2(String email_cliente2) {
        this.email_cliente2 = email_cliente2;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        //set view compenant
        masterLinLayaout = findViewById(R.id.check_master_layout);
        codcli_check_txtvw = findViewById(R.id.check_codcli_txtvw);
        cliente_check_txtvw = findViewById(R.id.check_cliente_txtvw);
        nf_check_txtvw = findViewById(R.id.check_nf_txtvw);
        startButton = findViewById(R.id.check_start_btn);
        devButton = findViewById(R.id.check_btn_dev);
        rentregaButton = findViewById(R.id.check_btn_rentrega);
        perButton = findViewById(R.id.check_btn_perfeito);
        //set the value of the items
        Intent intent = this.getIntent();
        args = intent.getExtras();
        codcli_check_txtvw.setText(args.getString("codcli"));
        cliente_check_txtvw.setText(args.getString("cliente"));
        nf_check_txtvw.setText(args.getString("nf"));
        email_cliente  = args.getString("email_cliente");
        position = args.getInt("position");
        numcar =(long) Methods.getSharedPref(this,"long",getString(R.string.SHcarga));
        numnota = Methods.longParser(args.getString("nf"));
        codcli = Methods.longParser(args.getString("codcli"));
        cliente = args.getString("cliente");
        masterLinLayaout.setOnTouchListener(this);
        startButton.setOnClickListener(this);
        devButton.setOnClickListener(this);
        rentregaButton.setOnClickListener(this);
        perButton.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.check_start_btn:
                Intent intent = new Intent(this,ProdutosActivity.class);
                startActivity(intent);
                Methods.setSharedPref(this,"long",getString(R.string.SHnota),numnota);
                Methods.setSharedPref(this,"long",getString(R.string.SHcodcli),codcli);
                Methods.setSharedPref(this,"string",getString(R.string.SHcliente),cliente);
                Methods.setSharedPref(this,"string",getString(R.string.SHemail_cliente),email_cliente);
                Methods.setSharedPref(this,"int",getString(R.string.SHnotaPosition),position);
                Activity nActivity =  NotasActivity.getInstance();
                nActivity.finish();
                finish();
                break;
            case R.id.check_btn_dev:
                pressLayout();
                devRentregaPer = 3;
                break;
            case R.id.check_btn_rentrega:
                pressLayout();
                devRentregaPer = 4;

                break;
            case R.id.check_btn_perfeito:
                pressLayout();
                devRentregaPer =1;
                break;
            case R.id.toast_btn_confirm:
                setUpNFDevRen(3,1);
                break;
            case R.id.toast_btn_dismiss:
                setUpNFDevRen(3,0);
                break;
        }
    }

    private String createGetDir(String numnota){
        File mediaStorageDir = getDir("records",MODE_PRIVATE);
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }
        if(numcar>0){
            String outputFile = mediaStorageDir.getAbsolutePath() +File.separator+String.valueOf(numcar)+"-"+numnota+".3gp";
            return outputFile;
        }
        return null;
    }
    @Override
    public void onBackPressed() {
        if(isDoubleBackClick){
            finish();
            if(outputFile!=null){
                File file = new File(outputFile);
                if(file.exists()){
                    file.delete();
                }
            }
        }else{
            View view = Methods.setToastView(CheckActivity.this,"",false,getString(R.string.canceling_checking),
                    true,"",false,"",false);
            Toast toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
            toast.setView(view);
            toast.show();
        }
        isDoubleBackClick = true;
        final Handler myHandler = new Handler();
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isDoubleBackClick = false;
                myHandler.removeCallbacksAndMessages(null);
            }
        },2000);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId()==R.id.check_master_layout){
            try{
                Methods.CloseSoftKeyboradOnTouch(this);
            }catch (NullPointerException ex){

            }
        }
        return false;
    }

    @Override
    public void onTaskFinish(String response) {
        if(response.trim().equals("ok")){
            nf.setStenvi(1);
        }
        //insert the nota into the local database
        saveNotaSqliteBG saveNotaSqliteBG = new saveNotaSqliteBG();
        saveNotaSqliteBG.execute();
    }


    public void setUpNFDevRen(final int stEnt, final int stCred){
        SingleShotLocationProvider.requestSingleUpdate(this, new SingleShotLocationProvider.LocationCallback() {
            @Override public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                if(devRentregaPer!=1 && devRentregaPer!=4){
                    alertDialog.dismiss();
                }
                Methods.showLoadingDialog(CheckActivity.this);
                //start new connetion to the database
                dbConnection = new DBConnection(CheckActivity.this);
                //set all the informatins to new nota fiscal
                nf = new NF();
                nf.setNumnota(Methods.longParser(String.valueOf(nf_check_txtvw.getText())));nf.setEmail_cliene2((email_cliente2!=null)?email_cliente2:"");
                nf.setObsentrega((obs!=null)?obs:"");
                nf.setDtent(Methods.getCurrentDate());
                nf.setLatent(Methods.roundFloat(location.latitude,6));
                nf.setLongtent(Methods.roundFloat(location.longitude,6));
                nf.setStenvi(0);
                nf.setStent(stEnt);
                nf.setStcred(stCred);
                //Try to save online into the server
                Methods.checkConnection(CheckActivity.this);
                String[] motivoCod = getResources().getStringArray(R.array.motivos_dev_cod);
                nf.setCodmotivo(Methods.integerParser(motivoCod[posSpinner]));
                String fileName = numcar + "-" + nf.getNumnota();
                if(Methods.isNetworkConnected){
                    String audio = Methods.getBase64FromPath(outputFile);
                    HashMap<String,String> map = Methods.stringToHashMap("NUMNOTA%NUMCAR%LAT%LONGT%DTENTREGA%OBSENT%EMAIL_CLIENTE%CODMOTIVO%STCRED%STATUS%AUDIO%FILENAME",
                            String.valueOf(nf.getNumnota()),String.valueOf(numcar),String.valueOf(nf.getLatent()),String.valueOf(nf.getLongtent()),nf.getDtent(),nf.getObsentrega(),
                            nf.getEmail_cliene2(),motivoCod[posSpinner], String.valueOf(nf.getStcred()),nf.getStent().toString(),audio,fileName);
                    try {
                        String encodedParams = Methods.encode(map);
                        SRVConnection connection = new SRVConnection(CheckActivity.this,null,"response");
                        connection.execute(getString(R.string.url_server_host)+getString(R.string.url_server_save_notadevren),encodedParams);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    nf.setStenvi(0);
                    saveNotaSqliteBG saveNotaSqliteBG = new saveNotaSqliteBG();
                    saveNotaSqliteBG.execute();
                }
            }
        });
    }
    private void sendToBCRecNotas(NF nf){
        Intent intent = new Intent("GETNFCHANGES");
        intent.putExtra("nf",nf.getEmail_cliene2());
        intent.putExtra("obsEntrega",nf.getObsentrega());
        intent.putExtra("stEnvi",nf.getStenvi());
        intent.putExtra("stEnt",nf.getStent());
        intent.putExtra("stCred",nf.getStcred());
        intent.putExtra("position",position);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
    }



    private void pressLayout(){
        RecordFrag recordFrag = new RecordFrag();
        outputFile = createGetDir(String.valueOf(nf_check_txtvw.getText()));
        Bundle args = new Bundle();
        args.putString("outputFile",outputFile);
        args.putString("email_cliente",email_cliente);
        args.putInt("whitchActivity",0);
        recordFrag.setArguments(args);
        recordFrag.show(getSupportFragmentManager(),"TRANSPORTCHECK"+numnota);
    }

    private void showGenerateCredit(){
        View view = Methods.setToastView(this,"",false,
                getString(R.string.credito_check), true,"Sim",true,
                "Não",true);
        Button btnConfirm = view.findViewById(R.id.toast_btn_confirm);
        btnConfirm.setOnClickListener(this);
        Button btnCancel= view.findViewById(R.id.toast_btn_dismiss);
        btnCancel.setOnClickListener(this);
        alertDialog = new AlertDialog.Builder(this)
                .setView(view).create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        btnConfirm.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }


    class saveNotaSqliteBG extends AsyncTask<Void, Void, Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean checkInsertNF = dbConnection.updatetNF(nf,"NUMCAR = ? AND NUMNOTA = ?",
                    new String[]{String.valueOf(numcar),String.valueOf(numnota)});
            return checkInsertNF;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean == true){
                //send atualization to the notas app
                sendToBCRecNotas(nf);
                //close the loading dialog
                Methods.closeLoadingDialog();
                //Close the dialog and the activity
                finish();
            }else{
                View view = Methods.setToastView(CheckActivity.this,"",false,getString(R.string.save_nfdevren_error),
                        true,"",false,"",false);
                Toast toast = Toast.makeText(CheckActivity.this, getString(R.string.save_nfdevren_error), Toast.LENGTH_SHORT);
                toast.setView(view);
                toast.show();
            }
        }
    }



    public void finalizar(){
        if(email_cliente2!=null && !email_cliente2.trim().matches("") && !Methods.isValidEmail(email_cliente2)){
            Methods.showEmailInvalidMsg(CheckActivity.this);
        }else{
            if(Methods.checkGPSTurndOn(CheckActivity.this,CheckActivity.this)) {
                switch (devRentregaPer) {
                    case 1:
                        setUpNFDevRen(1, 0);
                        break;
                    case 3:
                        showGenerateCredit();
                        break;
                    case 4:
                        setUpNFDevRen(4, 0);
                        break;
                }
            }
        }
    }
}