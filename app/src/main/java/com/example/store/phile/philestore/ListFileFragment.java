package com.example.store.phile.philestore;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by adityasridhar on 16-08-31.
 */
public class ListFileFragment extends ListFragment {
    private ArrayList<FileListItem> fileListItems = new ArrayList<FileListItem>();
    private ArrayAdapter<FileListItem> adapter;

    private Activity mAct;
    public interface FileActionsListener{
        public void onFileItemClicked(String itemPath);
        public void onFileItemSelected(int pos);
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

        if (mAct != null && ((MainActivity)mAct).getFileListItems() != null) {
            fileListItems = ((MainActivity)mAct).getFileListItems();
        }

        adapter = new ArrayAdapter<FileListItem>((MainActivity)mAct, R.layout.list_item, fileListItems) {
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

                FileListItem item = fileListItems.get(position);
                File file = new File(item.getFilePath() + item.getFileName());

                // Set list icon image
                ImageView iconImg = (ImageView) v.findViewById(R.id.list_icon);
                if (item.getIsSelected()) {
                    iconImg.setImageResource(R.drawable.checkmark_512_512);
                } else {
                    iconImg.setImageResource(R.drawable.file_512_512);
                    if (file.isDirectory()) {
                        iconImg.setImageResource(R.drawable.folder_512_512);
                    }
                }

                // Set list header text
                TextView headerText = (TextView) v.findViewById(R.id.list_heading);
                headerText.setText(item.getFileName());

                // Set list date text
                Date lastModifiedDate = new Date(file.lastModified());
                TextView dateText = (TextView) v.findViewById(R.id.list_date_created);
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy"); // hh:mm a
                dateText.setText(sdf.format(lastModifiedDate));

                return v;
            }
        };

        setListAdapter(adapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileListItem fileListItemClicked = fileListItems.get(position);
                String fullFilePath = fileListItemClicked.getFilePath() + fileListItemClicked.getFileName();
                File fileClicked = new File(fullFilePath);

                if (fileClicked.isDirectory()) {
                    try {
                        ((FileActionsListener) mAct).onFileItemClicked(fullFilePath);
                    } catch (ClassCastException cce) {

                    }
                }

                if (fileClicked.isFile()) {
                    // TODO: fire intent to open file
                }
            }
        });

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    ((FileActionsListener) mAct).onFileItemSelected(position);
                } catch (ClassCastException cce) {
                    return false;
                }

                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_file_fragment, container, false);
    }
}
