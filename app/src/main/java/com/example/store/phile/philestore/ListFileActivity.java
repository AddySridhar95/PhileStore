package com.example.store.phile.philestore;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by adityasridhar on 16-08-31.
 */
public class ListFileActivity extends ListActivity {
    private File path;
    private String[] files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO rename this to something else
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();

        if (extras != null && extras.getString("path") != null) {
            path = new File(extras.getString("path"));
        } else {
            path = new File(Environment.getExternalStorageDirectory().toString());
        }

        loadFiles();

        ListView lview = (ListView) findViewById(android.R.id.list);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, files);
        lview.setAdapter(adapter);
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {

        // TODO: fire intent to restart this activity only if the item selected is a directory.
        // TODO: If its a file, fire an intent to open the file

        String filename = files[position];
        if (path.toString().endsWith(File.separator)) {
            filename = path.toString() + filename;
        } else {
            filename = path.toString() + File.separator + filename;
        }

        Intent i = new Intent(this, ListFileActivity.class);
        i.putExtra("path", filename);
        startActivity(i);
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
