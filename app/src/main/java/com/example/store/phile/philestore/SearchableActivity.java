package com.example.store.phile.philestore;

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by adityasridhar on 16-09-17.
 */
public class SearchableActivity extends ListActivity {

    // Store default path
    private String path = Environment.getExternalStorageDirectory().toString();
    private ArrayList<SearchResultItem> allFiles = new ArrayList<>();

    private ArrayList<SearchResultItem> searchResults = new ArrayList<>();
    private ArrayList<SearchResultItem> searchResultsTmp = new ArrayList<>();
    private ArrayAdapter<SearchResultItem> adapter;
    private FilenameFilter filter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String filename) {
            File sel = new File(dir, filename);

            // Filters based on whether the file is hidden or not
            return (sel.isFile() || sel.isDirectory()) && !sel.isHidden();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("onCreate .... ", "oncccc");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        initializeAdapter();

        Intent intent = getIntent();
        if (intent != null){
            handleIntent(intent);
        }
    }

    private String normalizePath(String path) {
        if (!path.endsWith(File.separator)) {
            return path + File.separator;
        }

        return path;
    }

    private void populateFiles(String path) {
        File f = new File(path);

        if (!f.exists()) {
            return;
        }

        if (f.isFile()) {
            allFiles.add(new SearchResultItem(f.getName(), f.getAbsolutePath()));
            return;
        }

        // Its a directory
        allFiles.add(new SearchResultItem(f.getName(), f.getAbsolutePath()));
        String[] files = f.list(filter);
        for (int i = 0; i < files.length; i++) {
            populateFiles(normalizePath(f.getAbsolutePath()) + files[i]);
        }
    }

    private void searchFiles(String query) {
        for (int i = 0; i < allFiles.size(); i++) {
            File file = new File(allFiles.get(i).getPathName());

            if (file.getName().toLowerCase().contains(query.toLowerCase())) {
                searchResultsTmp.add(allFiles.get(i));
            }
        }
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            final String query = intent.getStringExtra(SearchManager.QUERY);
            path = intent.getStringExtra("PATH");

            AsyncTask a = new AsyncTask<String, Void, String>() {
                ProgressBar progress_bar = (ProgressBar) findViewById(R.id.search_progress_bar);
                TextView searchHeading = (TextView) findViewById(R.id.search_heading);

                public void onPreExecute() {
                    searchResultsTmp.clear();
                    if (progress_bar != null) {
                        progress_bar.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public String doInBackground(String... param) {
                    populateFiles(path);
                    searchFiles(query);
                    return "";
                }

                protected void onPostExecute(String result) {
                    searchResults.clear();
                    searchResults.addAll(searchResultsTmp);

                    adapter.notifyDataSetChanged();

                    Log.d("searchResults received", searchResults.size() + "");
                    if (progress_bar != null) {
                        progress_bar.setVisibility(View.GONE);
                    }

                    if (searchHeading != null) {
                        searchHeading.setText(String.format(getResources().getString(R.string.search_heading), searchResults.size(), query));
                    }
                }
            }.execute();
        }
    }

    public void initializeAdapter() {
        adapter = new ArrayAdapter<SearchResultItem>(this, R.layout.list_item, searchResults) {
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

                // If file list items is empty ..
                if (searchResults == null || searchResults.size() == 0) {
                    return v;
                }

                File file = new File(searchResults.get(position).getPathName());

                // Set list heading
                TextView listItem = (TextView) v.findViewById(R.id.list_heading);
                listItem.setText(searchResults.get(position).getFileName());

                // Set icon image
                ImageView iconImg = (ImageView) v.findViewById(R.id.list_icon);
                iconImg.setImageResource(R.drawable.file_512_512);
                if (file.isDirectory()) {
                    iconImg.setImageResource(R.drawable.folder_512_512);
                }

                // Hide sub heading
                RelativeLayout subHeading = (RelativeLayout) v.findViewById(R.id.list_sub_heading);
                subHeading.setVisibility(View.GONE);

                return v;
            }

        };

        setListAdapter(adapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchResultItem itemClicked = searchResults.get(position);
                String fullFilePath = itemClicked.getPathName();

                File f = new File(fullFilePath);

                if (f.isFile()) {
                    try {
                        FileOpen.openFile(getApplicationContext(), f);
                    } catch (IOException ex) {
                        showToast("Unable to open file");
                    }
                } else {
                    sendMainActivityIntent(itemClicked.getPathName());
                }
//                try {
//                    ((FileActionsListener) mAct).onFileItemClicked(fullFilePath);
//                } catch (ClassCastException cce) {
//
//                }
            }
        });
    }

    private void sendMainActivityIntent(String fullPath) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("SEARCH_PATH", fullPath);
        startActivity(intent);
    }

    private void showToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }
}
