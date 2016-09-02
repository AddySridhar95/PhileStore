package com.example.store.phile.philestore;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
    private ArrayAdapter<FileListItem> adapter;

    private Activity mAct;
    public interface OnFileItemSelectedListener{
        public void onFileItemSelected(String itemPath);
    }

    @Override
    public void onAttach(Context c)
    {
        super.onAttach(c);
        mAct = c instanceof Activity ? (Activity) c : null;

        Log.d("ListFileFragment", mAct == null ? "is null" : "not null");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new ArrayAdapter<FileListItem>((MainActivity)mAct, R.layout.list_item, fileItems) {
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
                iconImg.setImageResource(R.drawable.blank_48_48);
                if (file.isDirectory()) {
                    iconImg.setImageResource(R.drawable.folder_48_48);
                }

                TextView headerText = (TextView) v.findViewById(R.id.list_heading);
                headerText.setText(item.getFileName());

                Date lastModifiedDate = new Date(file.lastModified());
                TextView dateText = (TextView) v.findViewById(R.id.list_date_created);
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy hh:mm a");
                dateText.setText(sdf.format(lastModifiedDate));

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
                    try{
                        ((OnFileItemSelectedListener) mAct).onFileItemSelected(fullFilePath);
                    }catch (ClassCastException cce){

                    }
                }

                if (fileClicked.isFile()) {
                    // TODO: fire intent to open file
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_file_fragment, container, false);
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        loadFiles();
    }

    public void loadFiles() {
        String p = Environment.getExternalStorageDirectory().toString();
        if (mAct != null && ((MainActivity)mAct).getFullPath() != null) {
            p = ((MainActivity)mAct).getFullPath();
        } else {
            Log.d("ListFileFragment", "mAct is NULLLL");
        }

        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                File sel = new File(dir, filename);

                // Filters based on whether the file is hidden or not
                return (sel.isFile() || sel.isDirectory()) && !sel.isHidden();
            }
        };

        fileItems.clear();
        String[] files = (new File(p)).list(filter);
        for(int i = 0; i < files.length; i++) {

            // parentPath is the path of the directory the file in question is in.
            String parentPath = p;
            if (!parentPath.endsWith(File.separator)) {
                parentPath = parentPath + File.separator;
            }

            FileListItem fileListItem = new FileListItem(files[i], parentPath);
            fileItems.add(fileListItem);
            // adapter.notifyDataSetChanged();
        }

    }
}
