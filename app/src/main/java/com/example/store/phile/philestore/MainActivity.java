package com.example.store.phile.philestore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private File path;

    private String[] files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Log.d("MainActivity", Environment.getExternalStorageState());

        path = new File(Environment.getExternalStorageDirectory().toString());

        // Log.d("main_activity", path.getAbsolutePath());

        // Log.d("MainActivity", path.exists() ? "File exists" : "File doesn't exist");

//        try {
//            Boolean result = path.mkdirs();
//            Log.d("MainActivity", result ? "Success" : "Failure");
//        } catch (SecurityException e) {
//            Log.d("MainActivity", "SECURITY EXCEPTION");
//        }

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            loadFiles();
        }

        Log.d("MainActivity", "Loaded files " + files.length);
        ListView lview = (ListView) findViewById(R.id.list_view);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, files);
        lview.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    loadFiles();
                } else {
                    // TODO: permission denied ... quit app?
                    Log.d("MainActivity", "Permission denied");
                }

                return;
            }
        }
    }

    public void loadFiles() {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                File sel = new File(dir, filename);
                // Filters based on whether the file is hidden or not
                return (sel.isFile() || sel.isDirectory())
                        && !sel.isHidden();

            }
        };

        files = path.list(filter);

        if (files == null) {
            Log.d("MainActivity", "SD files is null");
        } else {
            for(int i = 0; i < files.length; i++) {
                Log.d("MainActivity", files[i]);
            }
        }
    }
}
