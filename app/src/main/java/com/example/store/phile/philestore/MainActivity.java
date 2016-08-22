package com.example.store.phile.philestore;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Log.d("MainActivity", Environment.getExternalStorageState());

        File path = new File(Environment.getExternalStorageDirectory().toString());

        Log.d("main_activity", path.getAbsolutePath());

        Log.d("MainActivity", path.exists() ? "File exists" : "File doesn't exist");

        try {
            Boolean result = path.mkdirs();
            Log.d("MainActivity", result ? "Success" : "Failure");
        } catch (SecurityException e) {
            Log.d("MainActivity", "SECURITY EXCEPTION");
        }

        Log.d("MainActivity", path.exists() ? "File exists" : "File doesn't exist");

        File[] sdFiles = path.listFiles();

        if (sdFiles == null) {
            Log.d("MainActivity", "SD files is null");
        } else {
            for(int i = 0; i < sdFiles.length; i++) {
                Log.d("MainActivity", sdFiles[i].getAbsolutePath());
            }
        }

    }
}
