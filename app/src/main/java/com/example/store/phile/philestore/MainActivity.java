package com.example.store.phile.philestore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import java.io.File;

public class MainActivity extends AppCompatActivity {

    private File path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Log.d("MainActivity", Environment.getExternalStorageState());

        path = new File(Environment.getExternalStorageDirectory().toString());

        Log.d("main_activity", path.getAbsolutePath());

        Log.d("MainActivity", path.exists() ? "File exists" : "File doesn't exist");

        try {
            Boolean result = path.mkdirs();
            Log.d("MainActivity", result ? "Success" : "Failure");
        } catch (SecurityException e) {
            Log.d("MainActivity", "SECURITY EXCEPTION");
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            // request permission

        } else {
            // TODO do success action here ?
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    File[] sdFiles = path.listFiles();

                    if (sdFiles == null) {
                        Log.d("MainActivity", "SD files is null");
                    } else {
                        for(int i = 0; i < sdFiles.length; i++) {
                            Log.d("MainActivity", sdFiles[i].getAbsolutePath());
                        }
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("MainActivity", "Permission denied");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
