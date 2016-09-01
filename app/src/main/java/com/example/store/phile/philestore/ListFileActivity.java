package com.example.store.phile.philestore;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by adityasridhar on 16-08-31.
 */
public class ListFileActivity extends ListActivity {
    private File path;
    private ArrayList<FileListItem> fileItems = new ArrayList<FileListItem>();

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

        ArrayAdapter<FileListItem> adapter = new ArrayAdapter<FileListItem>(this, R.layout.list_item, fileItems) {
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

                FileListItem item = fileItems.get(position);
                File file = new File(item.getFilePath() + item.getFileName());

                Date lastModifiedDate = new Date(file.lastModified());

                ImageView iconImg = (ImageView) v.findViewById(R.id.list_icon);
                iconImg.setImageResource(R.drawable.file_48_48);

                if (file.isDirectory()) {
                    iconImg.setImageResource(R.drawable.folder_48_48);
                }

                TextView headerText = (TextView) v.findViewById(R.id.list_heading);
                headerText.setText(item.getFileName());
                TextView dateText = (TextView) v.findViewById(R.id.list_date_created);
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy HH:mm");
                dateText.setText(sdf.format(lastModifiedDate));

                return v;
            }
        };
        lview.setAdapter(adapter);
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {

        FileListItem fileListItemClicked = fileItems.get(position);
        String fullFilePath = fileListItemClicked.getFilePath() + fileListItemClicked.getFileName();
        File fileClicked = new File(fullFilePath);

        if (fileClicked.isDirectory()) {
            Intent i = new Intent(this, ListFileActivity.class);
            i.putExtra("path", fullFilePath);
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

        String[] files = path.list(filter);
        for(int i = 0; i < files.length; i++) {
            String filePath = path.toString();
            if (!filePath.endsWith(File.separator)) {
                filePath = filePath + File.separator;
            }
            // Log.d("MainActivity", filePath + files[i]);
            FileListItem fileListItem = new FileListItem(files[i], filePath);
            fileItems.add(fileListItem);
        }
    }
}
