package com.karam.transport;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;

public class MainActivity extends AppCompatActivity  {
    private static final long SPLASH_SCREEN_MS = 1500;
    long gapTime;
    private long mTimeBeforeDelay;
    private Handler mSplashHandler;
    AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String TAG = getString(R.string.main_activity_tag);
        //mSplashHandler = new Handler();
        Dexter.withContext(this).withPermissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // The first time mTimeBeforeDelay will be 0.
        //gapTime = System.currentTimeMillis() - mTimeBeforeDelay;
        //if (gapTime > SPLASH_SCREEN_MS) {
        //    gapTime = SPLASH_SCREEN_MS;
        //}
        //if(Build.VERSION.SDK_INT >= 23){
        //    Dexter.withContext(this).withPermissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        //            Manifest.permission.ACCESS_COARSE_LOCATION,
        //            Manifest.permission.ACCESS_FINE_LOCATION)
        //            .withListener(new MultiplePermissionsListener() {
        //                @Override
        //                public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
        //                    //if(Methods.checkPlayServices(MainActivity.this,MainActivity.this)){
        //                        //setSplash();
        //                    //}
        //                    //if(multiplePermissionsReport.isAnyPermissionPermanentlyDenied()){
        //                    //    View view = Methods.setToastView(MainActivity.this,getString(R.string.permission_title),true,
        //                    //            getString(R.string.permission_camera), true,"Conceder",true,
        //                    //            "Negar",true);
        //                    //    Button btnConfirm = view.findViewById(R.id.toast_btn_confirm);
        //                    //    btnConfirm.setOnClickListener(MainActivity.this);
        //                    //    Button btnCancel= view.findViewById(R.id.toast_btn_dismiss);
        //                    //    btnCancel.setOnClickListener(MainActivity.this);
        //                    //    alertDialog = new AlertDialog.Builder(MainActivity.this)
        //                    //            .setView(view).create();
        //                    //    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //                    //    alertDialog.show();
        //                    //}
        //                    if (multiplePermissionsReport.areAllPermissionsGranted()){
        //                        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        //                        startActivity(intent);
        //                    }
        //                }
        //                @Override
        //                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
        //                    permissionToken.continuePermissionRequest();
        //                }
        //            }).check();
        //}else{
        //    //setSplash();
        //}
    }
    @Override
    protected void onPause() {
        super.onPause();
     //   mSplashHandler.removeCallbacksAndMessages(null);
    }
    //@Override
    //public void onClick(View v) {
    //    switch (v.getId()){
    //        case R.id.toast_btn_confirm:
    //            Intent intent = new Intent();
    //            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
    //            Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
    //            intent.setData(uri);
    //            startActivity(intent);
    //            break;
    //        case R.id.toast_btn_dismiss:
    //            alertDialog.dismiss();
    //            break;
    //    }
    //}
//
    //private void setSplash(){
    //    mSplashHandler.postDelayed(new Runnable() {
    //        @Override
    //        public void run() {
    //            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
    //            startActivity(intent);
    //            finish();
    //        }
    //    }, gapTime);
    //    // Save the time before the delay.
    //    mTimeBeforeDelay = System.currentTimeMillis();
    //}
}
