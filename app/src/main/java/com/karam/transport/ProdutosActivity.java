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

import java.util.ArrayList;

public class ProdutosActivity extends AppCompatActivity{
    TextView codcli_prod_txtvw, cliente_prod_txtvw, nf_prod_txtvw, prod_count_txtvw;
    ListView prodListView;
    ArrayList<Prod> prodList;
    boolean isDoubleBackClick = false;
    SearchView searchView;
    ProdAdapter prodAdapter;
    long numnota=0,codcli=0;
    private static final String tagMenu ="karamTransportItemCheck";
    String cliente="";
    int lisPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produtos);
        numnota =(long) Methods.getSharedPref(this,"long",getString(R.string.SHnota));
        codcli =(long) Methods.getSharedPref(this,"long",getString(R.string.SHcodcli));
        cliente = (String) Methods.getSharedPref(this,"string",getString(R.string.SHcliente));
        prodListView = findViewById(R.id.prod_listvw);

        codcli_prod_txtvw = findViewById(R.id.prod_codcli_txtvw);
        cliente_prod_txtvw = findViewById(R.id.prod_cliente_txtvw);
        nf_prod_txtvw = findViewById(R.id.prod_nf_txtvw);
        prod_count_txtvw = findViewById(R.id.prod_count_txtvw);
        //set the value of the items
        if(codcli!=0){
            codcli_prod_txtvw.setText(String.valueOf(codcli));
        }
        cliente_prod_txtvw.setText(cliente);
        if(numnota!=0){
            nf_prod_txtvw.setText(String.valueOf(numnota));
            LoadProdsBG loadProdsBG = new LoadProdsBG();
            loadProdsBG.execute(numnota);
        }

        //Add on click listener to the item of prodlist
        prodListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ProdutosActivity.this,CheckProdActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong("codprod",prodList.get(position).getCodprod());
                bundle.putString("descricao",prodList.get(position).getDescricao());
                bundle.putLong("qt",prodList.get(position).getQt());
                lisPosition = position;
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(barcodeReciever,new IntentFilter("GETBARCODEPROD"));
        LocalBroadcastManager.getInstance(this).registerReceiver(prodsReciever, new IntentFilter("GETPRODCHANGES"));
    }


    private BroadcastReceiver barcodeReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String barcode = intent.getStringExtra("barcode");
            for(Prod prod:prodList){
                if(barcode.matches(String.valueOf(prod.codbarra1))
                || barcode.matches(String.valueOf(prod.codbarra2))){
                    Intent intent2 = new Intent(ProdutosActivity.this,CheckProdActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("codprod",prod.getCodprod());
                    bundle.putString("descricao",prod.getDescricao());
                    bundle.putLong("qt",prod.getQt());
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
            prodList.get(lisPosition).setQtfalta(intent.getLongExtra("qtpend",0));
            prodList.get(lisPosition).setStdev(intent.getIntExtra("status",0));
            prodList.get(lisPosition).setCodmotivodev(intent.getIntExtra("codMotivoDev",0));
            prodAdapter.notifyDataSetChanged();
        }
    };


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.prod_menu_barcode:
                Intent intent3 = new Intent(this,BarcodeActivity.class);
                intent3.putExtra("chooser","prod");
                startActivity(intent3);
                break;
            case R.id.prod_menu_cancelar:
                DBConnection dbConnection = new DBConnection(this);
                Prod prod = new Prod();
                numnota =(long) Methods.getSharedPref(this,"long",getString(R.string.SHnota));
                prod.setQtfalta(null);
                prod.setCodmotivodev(null);
                prod.setStdev(null);
                SQLiteDatabase db = dbConnection.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put("QTFALTA",0l);
                contentValues.put("CODMOTIVO",0);
                contentValues.put("STDEV",0);
                db.update("PROD",contentValues,"NUMNOTA=?",new String[]{
                        String.valueOf(numnota)});
                Methods.setSharedPref(this,"long",getString(R.string.SHnota),0l);
                finish();
                Intent intentNF = new Intent(this,NotasActivity.class);
                startActivity(intentNF);
                break;
            case R.id.prod_menu_finalizar:
                break;
        }
        return true;
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



    class LoadProdsBG extends AsyncTask<Long, Void, ArrayList<Prod>> {
        @Override
        protected ArrayList<Prod> doInBackground(Long... longs) {
            DBConnection dbConnection = new DBConnection(ProdutosActivity.this);
            Long numnota = longs[0];
            prodList = new ArrayList<>();
            Cursor c = dbConnection.select(false, "PROD", new String[]{
                            "CODPROD", "QT","QTFALTA","STDEV", "CODBARRA1", "CODBARRA2", "DESCRICAO","CODMOTIVO"},
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
}
