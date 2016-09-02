package com.example.store.phile.philestore;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
public class ListFileFragment extends ListFragment {
    private ArrayList<FileListItem> fileItems = new ArrayList<FileListItem>();
    private String path;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayAdapter<FileListItem> adapter = new ArrayAdapter<FileListItem>(getActivity(), R.layout.list_item, fileItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // assign the view we are converting to a local variable
                View v = convertView;

                // TODO: why is this needed
                // first check to see if the view is null. if so, we have to inflate it.
                // to inflate it basically means to render, or show, the view.
                if (v == null) {
                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = inflater.inflate(R.layout.list_item, null);
                }

                FileListItem item = fileItems.get(position);
                File file = new File(item.getFilePath() + item.getFileName());

                ImageView iconImg = (ImageView) v.findViewById(R.id.list_icon);
                iconImg.setImageResource(R.drawable.file_48_48);
                if (file.isDirectory()) {
                    iconImg.setImageResource(R.drawable.folder_48_48);
                }

                TextView headerText = (TextView) v.findViewById(R.id.list_heading);
                headerText.setText(item.getFileName());

                Date lastModifiedDate = new Date(file.lastModified());
                TextView dateText = (TextView) v.findViewById(R.id.list_date_created);
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy HH:mm");
                dateText.setText(sdf.format(lastModifiedDate));

//                Log.d("MainAc", item.getFilePath() + item.getFileName());
//                StatFs stat = new StatFs(item.getFilePath() + item.getFileName());
//                long bytesAvailable = stat.getBlockSizeLong() * stat.getBlockCountLong();
//
//                TextView sizeText = (TextView) v.findViewById(R.id.list_size);
//                // String formattedSize = Formatter.formatShortFileSize(getContext(), bytesAvailable);
//                Log.d("MAINACTIVITY", bytesAvailable + "");
//                sizeText.setText(bytesAvailable / 1048576 + "");

                return v;
            }
        };

        setListAdapter(adapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileListItem fileListItemClicked = fileItems.get(position);
                String fullFilePath = fileListItemClicked.getFilePath() + fileListItemClicked.getFileName();
                File fileClicked = new File(fullFilePath);

                if (fileClicked.isDirectory()) {
                    MainActivity mainAct = (MainActivity) getActivity();
                    mainAct.onPathChange(fullFilePath);
//                    Intent i = new Intent(this, ListFileFragment.class);
//                    i.putExtra("path", fullFilePath);
//                    startActivity(i);
                }

                if (fileClicked.isFile()) {
                    // TODO: fire intent to open file
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getArguments() != null && getArguments().getString("path") != null) {
            path = getArguments().getString("path");
        } else {
            path = Environment.getExternalStorageDirectory().toString();
        }

        loadFiles();

        return inflater.inflate(R.layout.list_file_fragment, container, false);
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

        String[] files = (new File(path)).list(filter);

        for(int i = 0; i < files.length; i++) {

            // parentPath is the path of the directory the file in question is in.
            String parentPath = path;
            if (!parentPath.endsWith(File.separator)) {
                parentPath = parentPath + File.separator;
            }

            FileListItem fileListItem = new FileListItem(files[i], parentPath);
            fileItems.add(fileListItem);
        }
    }
}
