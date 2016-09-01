package com.example.store.phile.philestore;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

        // TODO: set content view to empty files view if files.length == 0 here

        ListView lview = (ListView) findViewById(android.R.id.list);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, files) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // assign the view we are converting to a local variable
                View v = convertView;

                // first check to see if the view is null. if so, we have to inflate it.
                // to inflate it basically means to render, or show, the view.
                if (v == null) {
                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = inflater.inflate(R.layout.list_item, null);
                }

                String item = files[position];

                TextView text1 = (TextView) v.findViewById(android.R.id.text1);
                text1.setText("abc");
                TextView text2 = (TextView) v.findViewById(android.R.id.text2);
                text2.setText(item);

                return v;
            }
        };
        lview.setAdapter(adapter);
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {

        String filename = files[position];
        if (path.toString().endsWith(File.separator)) {
            filename = path.toString() + filename;
        } else {
            filename = path.toString() + File.separator + filename;
        }

        File fileClicked = new File(filename);

        if (fileClicked.isDirectory()) {
            Intent i = new Intent(this, ListFileActivity.class);
            i.putExtra("path", filename);
            startActivity(i);
        }

        if (fileClicked.isFile()) {
            // TODO: fire intent to open file
        }
    }

    public void loadFiles() {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                File sel = new File(dir, filename);
                // Filters based on whether the file is hidden or not
                return (sel.isFile() || sel.isDirectory()) && !sel.isHidden();
            }
        };

        files = path.list(filter);
    }
}
