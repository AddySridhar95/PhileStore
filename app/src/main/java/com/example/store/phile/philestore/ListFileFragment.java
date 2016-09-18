package com.example.store.phile.philestore;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by adityasridhar on 16-08-31.
 */
public class ListFileFragment extends ListFragment {

    public interface FileActionsListener{
        public void onFileItemClicked(String itemPath);
        public void onFileItemSelected(int pos);
    }

    private ArrayList<FileListItem> fileListItems = new ArrayList<>();
    private ArrayList<FileListItem> fileListItemsBuffer = new ArrayList<>();
    private ArrayList<FileListItem> clipboard = new ArrayList<>();
    private String clipboardOperation = "move";

    private ArrayAdapter<FileListItem> adapter;
    private Activity mAct;
    private FilenameFilter filter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String filename) {
            File sel = new File(dir, filename);

            // Filters based on whether the file is hidden or not
            return (sel.isFile() || sel.isDirectory()) && !sel.isHidden();
        }
    };
    @Override
    public void onAttach(Context c)
    {
        super.onAttach(c);
        mAct = c instanceof Activity ? (Activity) c : null;

        Log.d("ListFileFragment", mAct == null ? "is null" : "not null");
    }

    private String getFileSizeText(File f) {
        if (f.isDirectory()) {
            int items = f.list().length;
            return items + " items";
        } else {
            return android.text.format.Formatter.formatShortFileSize(mAct, f.length());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d("ListFileFragment", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        adapter = new ArrayAdapter<FileListItem>((MainActivity)mAct, R.layout.list_item, fileListItems) {
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
                if (fileListItems == null || fileListItems.size() == 0) {
                    return v;
                }

                FileListItem item = fileListItems.get(position);
                File file = new File(item.getFullPath());

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
                TextView dateText = (TextView) v.findViewById(R.id.list_date_created);
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy"); // hh:mm a
                dateText.setText(sdf.format(item.getLastModified()));

                // Set list size text
                TextView sizeText = (TextView) v.findViewById(R.id.list_size);
                sizeText.setText(getFileSizeText(file));
                return v;
            }

        };

        setListAdapter(adapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileListItem fileListItemClicked = fileListItems.get(position);
                String fullFilePath = fileListItemClicked.getFullPath();

                try {
                    ((FileActionsListener) mAct).onFileItemClicked(fullFilePath);
                } catch (ClassCastException cce) {

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

        AsyncTask a = new AsyncTask<String, Void, String>() {
            ProgressBar progress_bar = (ProgressBar) getView().findViewById(R.id.progress_bar);

            public void onPreExecute() {
                if (progress_bar != null) {
                    Log.d("NULLLL", "Progress bar is null");
                    progress_bar.setVisibility(View.VISIBLE);
                }

                fileListItems.clear();
                adapter.notifyDataSetChanged();
            }

            @Override
            public String doInBackground(String... param) {
                prepareFileItemsFromPath();

                return "";
            }

            protected void onPostExecute(String result) {
                updateFileListItems();

                if (progress_bar != null) {
                    progress_bar.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    public void updateFileListItems() {
        Log.d("this is the thing", fileListItemsBuffer.size() + "");
        fileListItems.clear();
        fileListItems.addAll(fileListItemsBuffer);
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_file_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /*
     * Unselectes all file items
     */
    public void unselectAllFileItems() {
        for (int i = 0; i < fileListItems.size(); i++) {
            fileListItems.get(i).setIsSelected(false);
        }
        adapter.notifyDataSetChanged();
    }

    /*
     * Selected file becomes unselected. Unselected file becomes selected
     */
    public void toggleFileItemSelected(int pos) {
        if (fileListItems != null && fileListItems.size() >= pos + 1 && pos >= 0) {
            boolean isSelected = fileListItems.get(pos).getIsSelected();
            fileListItems.get(pos).setIsSelected(!isSelected);
            adapter.notifyDataSetChanged();
        }
    }

    /*
     * Deletes all file items selected.
     */
    public void deleteFileItems() {
        ArrayList<FileListItem> fileItemsSelected = getFileItemsSelected();


        for (int i = 0; i < fileItemsSelected.size(); i++) {
            FileListItem toBeDeleted = fileItemsSelected.get(i);
            File fileToBeDeleted = new File(toBeDeleted.getFullPath());

            boolean deleteStatus = deleteFile(fileToBeDeleted);

            if (!deleteStatus) {
                showToast("Delete failed");
            }

            // hack!!
            fileItemsSelected.get(i).setIsSelected(false);
            adapter.notifyDataSetChanged();
        }
    }

    public void renameFileItem(String targetFileName) {
        ArrayList<FileListItem> fileItemsSelected = getFileItemsSelected();

        FileListItem toBeRenamed = fileItemsSelected.get(0);
        File fileToBeRenamed = new File(toBeRenamed.getFullPath());
        File fileToBeRenamedTo = new File(normalizeFilePaths(toBeRenamed.getFilePath()) + targetFileName);

        boolean renameStatus = renameFile(fileToBeRenamed, fileToBeRenamedTo);

        if (!renameStatus) {
            showToast("Rename failed");
        }

        // hack!!
        fileItemsSelected.get(0).setIsSelected(false);
        adapter.notifyDataSetChanged();
    }

    public void moveFileItems() {
        ArrayList<FileListItem> fileItemsSelected = getFileItemsSelected();

        clipboard = fileItemsSelected;
        clipboardOperation = "move";

        // Mark each selected item as unselected
        for(int i = 0; i < fileItemsSelected.size(); i++) {
            fileItemsSelected.get(i).setIsSelected(false);
            adapter.notifyDataSetChanged();
        }
    }

    public void copyFileItems() {
        ArrayList<FileListItem> fileItemsSelected = getFileItemsSelected();

        clipboard = fileItemsSelected;
        clipboardOperation = "copy";

        // Mark each selected item as unselected
        for(int i = 0; i < fileItemsSelected.size(); i++) {
            fileItemsSelected.get(i).setIsSelected(false);
            adapter.notifyDataSetChanged();
        }
    }

    public void pasteFileItems() {
        showToast("Hold on! This might take a while.");

        for(int i = 0; i < clipboard.size(); i++) {
            FileListItem clipboardFileItem = clipboard.get(i);
            String parentPath = normalizeFilePaths(((MainActivity) mAct).getPath());

            File clipboardFile = new File(clipboardFileItem.getFullPath());
            File targetFile = new File(parentPath + clipboardFileItem.getFileName());

            Log.d("clipboardFile", clipboardFile.getAbsolutePath());
            Log.d("targetFile", targetFile.getAbsolutePath());

            if (clipboardOperation.equals("move")) {
                boolean status = renameFile(clipboardFile, targetFile);

                if (!status) {
                    showToast("Failed to paste " + clipboardFileItem.getFileName());
                }
            } else {

                boolean status = copyFilesOrDirectories(clipboardFile, targetFile);

                if (!status) {
                    showToast("Failed to paste " + clipboardFileItem.getFileName());
                }
            }
        }

        clipboard.clear();
    }

    // --- Helpers -------

    private boolean renameFile(File from, File to) {
        return from.exists() && !to.exists() && from.renameTo(to);
    }

    /*
     * Helper function to delete a File object. File might be a directory, so needs to clean up
     * its contents recursively
     */
    private boolean deleteFile(File file) {

        if (!file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            String[] contents = file.list(filter);
            for (int i = 0; i < contents.length; i++) {

                // TODO error handling
                boolean a = deleteFile(new File(file.getAbsolutePath(), contents[i]));
            }
        }
        return file.delete();
    }

    /*
     * Lists all files in a directory path and formulates file item objects
     */
    private void prepareFileItemsFromPath() {
        fileListItemsBuffer.clear();
        String path = ((MainActivity)mAct).getPath();

        String[] files = (new File(path)).list(filter);

        if (files == null) {
            return;
        }

        for(int i = 0; i < files.length; i++) {
            // parentPath is the path of the directory the file in question is in.
            String parentPath = path;
            if (!parentPath.endsWith(File.separator)) {
                parentPath = parentPath + File.separator;
            }

            File file = new File(parentPath + files[i]);
            long rawSize = getRawFileSize(file);
            Date lastModified = new Date(file.lastModified());
            FileListItem fileListItem = new FileListItem(files[i], parentPath, rawSize, lastModified);

            fileListItemsBuffer.add(fileListItem);
        }

        Log.d("prepareFileIt", fileListItemsBuffer.size() + "");

        // TODO: bug. sort should sort fileListItemsBuffer and not fileListItems ???
        sortFileListItems();
    }

    private void sortFileListItems() {
        int sortOptionIndexSelected = ((MainActivity)mAct).getSortOptionIndexSelected();
        boolean sortOrderIsAscending = ((MainActivity)mAct).getSortOrderIsAscending();

        if (sortOptionIndexSelected == 0) {
            Collections.sort(
                    fileListItemsBuffer,
                    sortOrderIsAscending ? FileListItem.FileNameComparatorAsc : FileListItem.FileNameComparatorDesc
            );
        }

        if (sortOptionIndexSelected == 1) {
            Collections.sort(
                    fileListItemsBuffer,
                    sortOrderIsAscending ? FileListItem.FileRawSizeComparatorAsc : FileListItem.FileRawSizeComparatorDesc
            );
        }

        if (sortOptionIndexSelected == 2) {
            Collections.sort(
                    fileListItemsBuffer,
                    sortOrderIsAscending ? FileListItem.FileDateComparatorAsc : FileListItem.FileDateComparatorDesc
            );
        }
    }

    private long getRawFileSize(File f) {
        if (f.isFile()) {
            return f.length();
        } else {
            String[] children = f.list(filter);
            long len = 0;
            for (int i = 0; i < children.length; i++) {
                String parentPath = f.getAbsolutePath();
                if (!parentPath.endsWith(File.separator)) {
                    parentPath = parentPath + File.separator;
                }

                len = len + getRawFileSize(new File(parentPath + children[i]));
            }

            return len;
        }
    }

    private String normalizeFilePaths(String path) {
        if (!path.endsWith(File.separator)) {
            return path + File.separator;
        }

        return path;
    }

    private boolean copyFilesOrDirectories(File from, File to) {
        if (!from.exists()) {
            return false;
        }

        if (from.isDirectory()) {
            if (!to.exists()) {
                to.mkdir();
            }

            String[] files = from.list(filter);
            // Log.d("copyFilesOrDirectories", files.length + "");
            for(int i = 0; i < files.length; i++) {

                // parentPath is the path of the directory the file in question is in.
                String parentPath = normalizeFilePaths(from.getAbsolutePath());
                final File fromFile = new File(parentPath + files[i]);
                final File toFile = new File(to.getAbsolutePath(), files[i]);

                boolean a = copyFilesOrDirectories(fromFile, toFile);

                // TODO error handling

            }

            Log.d("copyFilesOrDirectories", "directory!!!!");
            return true;
        } else {
            Log.d("copyFilesOrDirectories", "file!!!!");
            final File f = from;
            final File t = to;

                AsyncTask b = new AsyncTask<String, Void, String>() {
                    ProgressBar progress_bar = (ProgressBar) getView().findViewById(R.id.progress_bar);

                    public void onPreExecute() {
                        Log.d("async task b", "on pre execute");
                        if (progress_bar != null) {
                            progress_bar.setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public String doInBackground(String... param) {
                        try {
                            copy(f, t);
                        } catch (IOException ex) {
                            showToast("Unable to copy file");
                        }
                        return "";
                    }

                    protected void onPostExecute(String result) {
                        if (progress_bar != null) {
                            progress_bar.setVisibility(View.GONE);
                        }
                    }
                }.execute();

                return true;
        }
    }

    private void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private void showToast(String text) {
        Toast toast = Toast.makeText(mAct, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    // ---- Getters ---

    public ArrayList<FileListItem> getFileItemsSelected() {
        ArrayList<FileListItem> fileListItemsSelected = new ArrayList<>();
        for (int i = 0; i < fileListItems.size(); i++) {
            if (fileListItems.get(i).getIsSelected()) {
                fileListItemsSelected.add(fileListItems.get(i));
            }
        }

        return fileListItemsSelected;
    }

    public int getNoFileItemsSelected() {
        return getFileItemsSelected().size();
    }

    public String getClipboardOperation() {
        return clipboardOperation;
    }

    public ArrayList<FileListItem> getClipboard() {
        return clipboard;
    }
}
