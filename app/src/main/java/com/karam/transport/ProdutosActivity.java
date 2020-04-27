package com.karam.transport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class ProdutosActivity extends AppCompatActivity implements TaskListenerAct{
    TextView codcli_prod_txtvw, cliente_prod_txtvw, nf_prod_txtvw, prod_count_txtvw;
    ListView prodListView;
    ArrayList<Prod> prodList,prodListPend;
    boolean isDoubleBackClick = false;
    SearchView searchView;
    ProdAdapter prodAdapter;
    long numnota = 0, codcli = 0, numcar = 0;
    private static final String tagMenu = "karamTransportItemCheck";
    String cliente = "", outputFile = "", email_cliente = "", email_cliente2, obs;
    int lisPosition,notaPosition;
    DBConnection dbConnection;
    NF nf;
    int stredito =0,motivoPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produtos);
        numcar = (long) Methods.getSharedPref(this, "long", getString(R.string.SHcarga));
        numnota = (long) Methods.getSharedPref(this, "long", getString(R.string.SHnota));
        codcli = (long) Methods.getSharedPref(this, "long", getString(R.string.SHcodcli));
        email_cliente = (String) Methods.getSharedPref(this, "string", getString(R.string.SHemail_cliente));
        cliente = (String) Methods.getSharedPref(this, "string", getString(R.string.SHcliente));
        notaPosition = (int) Methods.getSharedPref(this,"int",getString(R.string.SHnotaPosition));
        prodListView = findViewById(R.id.prod_listvw);


        codcli_prod_txtvw = findViewById(R.id.prod_codcli_txtvw);
        cliente_prod_txtvw = findViewById(R.id.prod_cliente_txtvw);
        nf_prod_txtvw = findViewById(R.id.prod_nf_txtvw);
        prod_count_txtvw = findViewById(R.id.prod_count_txtvw);
        //set the value of the items
        if (codcli != 0) {
            codcli_prod_txtvw.setText(String.valueOf(codcli));
        }
        cliente_prod_txtvw.setText(cliente);
        if (numnota != 0) {
            nf_prod_txtvw.setText(String.valueOf(numnota));
            LoadProdsBG loadProdsBG = new LoadProdsBG();
            loadProdsBG.execute(numnota);
        }

        //Add on click listener to the item of prodlist
        prodListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ProdutosActivity.this, CheckProdActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong("codprod", prodList.get(position).getCodprod());
                bundle.putString("descricao", prodList.get(position).getDescricao());
                bundle.putLong("qt", prodList.get(position).getQt());
                lisPosition = position;
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(barcodeReciever, new IntentFilter("GETBARCODEPROD"));
        LocalBroadcastManager.getInstance(this).registerReceiver(prodsReciever, new IntentFilter("GETPRODCHANGES"));
    }


    private BroadcastReceiver barcodeReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String barcode = intent.getStringExtra("barcode");
            for (Prod prod : prodList) {
                if (barcode.matches(String.valueOf(prod.codbarra1))
                        || barcode.matches(String.valueOf(prod.codbarra2))) {
                    Intent intent2 = new Intent(ProdutosActivity.this, CheckProdActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("codprod", prod.getCodprod());
                    bundle.putString("descricao", prod.getDescricao());
                    bundle.putLong("qt", prod.getQt());
                    intent2.putExtras(bundle);
                    startActivity(intent2);
                    return;
                }
            }
            View view = Methods.setToastView(ProdutosActivity.this, "", false, getString(R.string.invalid_barcode_nf),
                    true, "", false, "", false);
            Toast toast = Toast.makeText(ProdutosActivity.this, "", Toast.LENGTH_SHORT);
            toast.setView(view);
            toast.show();
        }
    };


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.prods, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem search_item_menu = menu.findItem(R.id.prod_menu_search);
        searchView = (SearchView) search_item_menu.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                prodAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }


    private BroadcastReceiver prodsReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            prodList.get(lisPosition).setQtfalta(intent.getLongExtra("qtpend", 0));
            prodList.get(lisPosition).setStdev(intent.getIntExtra("status", 0));
            prodList.get(lisPosition).setCodmotivodev(intent.getIntExtra("codMotivoDev", 0));
            prodAdapter.notifyDataSetChanged();
        }
    };


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.prod_menu_barcode:
                Intent intent3 = new Intent(this, BarcodeActivity.class);
                intent3.putExtra("chooser", "prod");
                startActivity(intent3);
                break;
            case R.id.prod_menu_cancelar:
                DBConnection dbConnection = new DBConnection(this);
                Prod prod = new Prod();
                numnota = (long) Methods.getSharedPref(this, "long", getString(R.string.SHnota));
                prod.setQtfalta(null);
                prod.setCodmotivodev(null);
                prod.setStdev(null);
                SQLiteDatabase db = dbConnection.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put("QTFALTA", 0l);
                contentValues.put("CODMOTIVO", 0);
                contentValues.put("STDEV", 0);
                db.update("PROD", contentValues, "NUMNOTA=?", new String[]{
                        String.valueOf(numnota)});
                Methods.setSharedPref(this,"long",getString(R.string.SHnota),0l);
                Methods.setSharedPref(this,"long",getString(R.string.SHcodcli),0l);
                Methods.setSharedPref(this,"string",getString(R.string.SHcliente),"");
                Methods.setSharedPref(this,"string",getString(R.string.SHemail_cliente),"");
                Methods.setSharedPref(this,"int",getString(R.string.SHnotaPosition),-1);
                finish();
                Intent intentNF = new Intent(this, NotasActivity.class);
                startActivity(intentNF);
                break;
            case R.id.prod_menu_finalizar:
                outputFile = createGetDir(String.valueOf(numnota));
                RecordFrag recordFrag = new RecordFrag();
                Bundle args = new Bundle();
                args.putString("outputFile", outputFile);
                args.putString("email_cliente", email_cliente);
                args.putInt("whitchActivity", 1);
                recordFrag.setArguments(args);
                recordFrag.show(getSupportFragmentManager(), "TRANSPORTCHECK" + numnota);
                break;
        }
        return true;
    }

    private String createGetDir(String numnota) {
        File mediaStorageDir = getDir("records", MODE_PRIVATE);
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }
        if (numcar > 0) {
            String outputFile = mediaStorageDir.getAbsolutePath() + File.separator + String.valueOf(numcar) + "-" + numnota + ".3gp";
            return outputFile;
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        if (isDoubleBackClick) {
            finish();
        } else {
            View view = Methods.setToastView(ProdutosActivity.this, "", false, getString(R.string.closing_alert),
                    true, "", false, "", false);
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
        }, 2000);
    }

    @Override
    public void onTaskFinish(String response) {

    }


    class LoadProdsBG extends AsyncTask<Long, Void, ArrayList<Prod>> {
        @Override
        protected ArrayList<Prod> doInBackground(Long... longs) {
            DBConnection dbConnection = new DBConnection(ProdutosActivity.this);
            Long numnota = longs[0];
            prodList = new ArrayList<>();
            Cursor c = dbConnection.select(false, "PROD", new String[]{
                            "CODPROD", "QT", "QTFALTA", "STDEV", "CODBARRA1", "CODBARRA2", "DESCRICAO", "CODMOTIVO"},
                    "NUMNOTA=?", new String[]{String.valueOf(numnota)}
                    , null, null, "DESCRICAO", null);
            if (c != null) {
                c.moveToFirst();
                while (c != null && c.isAfterLast() == false) {
                    Prod prod = new Prod();
                    try {
                        prod.setCodprod(c.getLong(c.getColumnIndex("CODPROD")));
                        prod.setQt(c.getLong(c.getColumnIndex("QT")));
                        prod.setCodbarra1(c.getLong(c.getColumnIndex("CODBARRA1")));
                        prod.setCodbarra2(c.getLong(c.getColumnIndex("CODBARRA2")));
                        prod.setDescricao(c.getString(c.getColumnIndex("DESCRICAO")));
                        prod.setQtfalta(c.getLong(c.getColumnIndex("QTFALTA")));
                        prod.setStdev(c.getInt(c.getColumnIndex("STDEV")));
                        prod.setCodmotivodev(c.getInt(c.getColumnIndex("CODMOTIVO")));
                    } catch (NumberFormatException ex) {

                    }
                    prodList.add(prod);
                    c.moveToNext();
                }
            } else {
                return null;
            }
            c.close();
            return prodList;
        }

        @Override
        protected void onPostExecute(ArrayList<Prod> prods) {
            try {
                prodAdapter = new ProdAdapter(ProdutosActivity.this, prods);
                prodListView.setAdapter(prodAdapter);
                prod_count_txtvw.setText(prodAdapter.getCount() + " Produtos");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setEmail_cliente2(String email_cliente2) {
        this.email_cliente2 = email_cliente2;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }


    public void finalizar(){
        SingleShotLocationProvider.requestSingleUpdate(this, new SingleShotLocationProvider.LocationCallback() {
            @Override
            public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                Methods.showLoadingDialog(ProdutosActivity.this);
                String jsonArr="";
                //start new connetion to the database
                dbConnection = new DBConnection(ProdutosActivity.this);
                //set all the informatins to new nota fiscal
                nf = new NF();
                nf.setNumnota(numnota);
                nf.setObsentrega((obs != null) ? obs : "");
                nf.setDtent(Methods.getCurrentDate());
                nf.setLatent(Methods.roundFloat(location.latitude, 4));
                nf.setLongtent(Methods.roundFloat(location.longitude, 4));
                nf.setStenvi(0);
                nf.setStcred(stredito);
                //Add arraylist of hashmap
                ArrayList<HashMap<String,String>> mapArr = new ArrayList<>();
                //check the type of the nota
                Cursor c = dbConnection.select(false, "PROD", new String[]{
                                "CODPROD", "QT", "QTFALTA", "STDEV", "CODBARRA1", "CODBARRA2", "DESCRICAO", "CODMOTIVO"},
                        "NUMNOTA=? AND STDEV>?", new String[]{String.valueOf(numnota)
                                , "0"}
                        , null, null, null, null);
                int rowsCount = c.getCount();
                if (rowsCount == 0) {
                    nf.setStent(1);
                    jsonArr = "";
                } else {
                    nf.setStent(2);

                    c.moveToFirst();
                    while(c!=null && !c.isAfterLast()){
                        HashMap<String,String> map = new HashMap<>();
                        Prod prod = new Prod();
                        map.put("CODPROD",String.valueOf(c.getLong(c.getColumnIndex("CODPROD"))));
                        prod.setCodprod(c.getLong(c.getColumnIndex("CODPROD")));
                        map.put("QTFALTA",String.valueOf(c.getLong(c.getColumnIndex("QTFALTA"))));
                        prod.setQtfalta(c.getLong(c.getColumnIndex("QTFALTA")));
                        map.put("CODMOTIVO",String.valueOf(c.getInt(c.getColumnIndex("CODMOTIVO"))));
                        prod.setCodmotivodev(c.getInt(c.getColumnIndex("CODMOTIVO")));
                        map.put("STDEV",String.valueOf(c.getInt(c.getColumnIndex("STDEV"))));
                        prod.setStdev(c.getInt(c.getColumnIndex("STDEV")));
                        prodListPend.add(prod);
                        mapArr.add(map);
                        c.moveToNext();
                    }
                    c.close();
                    jsonArr = Methods.toJson(mapArr);
                }
                //Try to save online into the server
                Methods.checkConnection(ProdutosActivity.this);
                String[] motivoCod = getResources().getStringArray(R.array.motivos_dev_cod);
                String fileName = numcar + "-" + nf.getNumnota();
                if (Methods.isNetworkConnected) {
                    String audio = Methods.getBase64FromPath(outputFile);
                    HashMap<String, String> map = Methods.stringToHashMap("NUMNOTA%NUMCAR%LAT%LONGT%DTENTREGA%OBSENT%EMAIL_CLIENTE%CODMOTIVO%STCRED%STATUS%AUDIO%FILENAME%PRODJSON",
                            nf.getNumnota().toString(), String.valueOf(numcar), nf.getLatent().toString(), nf.getLongtent().toString(), nf.getDtent(), nf.getObsentrega(),
                            nf.getEmail_cliene2(),motivoCod[motivoPos] , nf.getStcred().toString(), nf.getStent().toString(), audio, fileName,jsonArr);
                    try {
                        String encodedParams = Methods.encode(map);
                        SRVConnection connection = new SRVConnection(ProdutosActivity.this, null, "response");
                        connection.execute(getString(R.string.url_server_host) + getString(R.string.url_server_save_notadevren), encodedParams);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                nf.setStenvi(0);
                saveSqliteBG saveSqliteBG = new saveSqliteBG();
                saveSqliteBG.execute();
                }
            }
        });
    }

    class saveSqliteBG extends AsyncTask<Void, Void, Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean checkInsertNF = dbConnection.updatetNF(nf,"NUMNOTA="+nf.getNumnota(),null);
            for(Prod prod:prodListPend){
                dbConnection.updateProd(prod,"NUMNOTA=? AND CODPROD=?",new String[]{
                        String.valueOf(nf.getNumnota()) , String.valueOf(prod.getCodprod())
                });
            }
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
                View view = Methods.setToastView(ProdutosActivity.this,"",false,getString(R.string.save_nfdevren_error),
                        true,"",false,"",false);
                Toast toast = Toast.makeText(ProdutosActivity.this, getString(R.string.save_nfdevren_error), Toast.LENGTH_SHORT);
                toast.setView(view);
                toast.show();
            }
        }
    }


    private void sendToBCRecNotas(NF nf){
        Intent intent = new Intent("GETNFCHANGES");
        intent.putExtra("nf",nf.getEmail_cliene2());
        intent.putExtra("obsEntrega",nf.getObsentrega());
        intent.putExtra("stEnvi",nf.getStenvi());
        intent.putExtra("stEnt",nf.getStent());
        intent.putExtra("stCred",nf.getStcred());
        intent.putExtra("position",notaPosition);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
    }
}