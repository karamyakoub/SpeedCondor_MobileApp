package com.karam.transport;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    Boolean isLogin;
    Boolean  isDoubleBackClick = false;
    long numcar,numnota;
    RelativeLayout containerLayout;
    LinearLayout sideLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String TAG = getString(R.string.login_activity);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        containerLayout = findViewById(R.id.login_container_layout);
        Fragment fragment;
        isLogin =(Boolean) Methods.getSharedPref(LoginActivity.this,"boolean",getString(R.string.SHisLogin));
        numcar = (long) Methods.getSharedPref(LoginActivity.this,"long",getString(R.string.SHcarga));
        numnota =(long) Methods.getSharedPref(LoginActivity.this,"long",getString(R.string.SHnota));
        if(isLogin == true){
            if(numcar != 0 ){
                if(numnota != 0){
                    Intent intent = new Intent(this,ProdutosActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Intent intent = new Intent(this,NotasActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            else
            {
                fragment = new LoginCarregFrag();
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.login_container_layout,fragment).addToBackStack(null).commit();
            }
        }
        else
        {
            fragment = new LoginMainFrag();
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.login_container_layout,fragment).addToBackStack(null).commit();
        }
    }
    @Override
    public void onBackPressed() {
        if(isDoubleBackClick){
            finish();
        }else{
            View view = Methods.setToastView(LoginActivity.this,"",false,getString(R.string.closing_alert),
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
}