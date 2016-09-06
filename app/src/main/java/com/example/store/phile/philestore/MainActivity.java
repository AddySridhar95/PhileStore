package com.example.store.phile.philestore;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements ListFileFragment.FileActionsListener, TabViewFragment.TabActionsListener {

    private String path = Environment.getExternalStorageDirectory().toString();
    private String undisturbedPath = path;
    private ArrayList<FileListItem> fileListItems = new ArrayList<>();
    private ArrayList<FileListItem> clipboard = new ArrayList<>();
    private String clipboardOperation = "move";

    private Toolbar myToolbar;
    private FilenameFilter filter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String filename) {
            File sel = new File(dir, filename);

            // Filters based on whether the file is hidden or not
            return (sel.isFile() || sel.isDirectory()) && !sel.isHidden();
        }
    };

    // --------- Handlers --------------

    @Override
    public void onFileItemClicked(String p) {
        path = p;
        undisturbedPath = p;
        prepareFileItemsFromPath();
        restartListFragment();
    }

    @Override
    public void onFileItemSelected(int pos) {
        FileListItem fileItemSelected = fileListItems.get(pos);
        fileItemSelected.setIsSelected(!fileItemSelected.getIsSelected());
        restartListFragment();
        invalidateOptionsMenu();
    }

    @Override
    public void onTabItemClicked(String itemPath) {
        Log.d("onTabItemClicked", itemPath);
        path = itemPath;
        prepareFileItemsFromPath();
        restartListFragment();
        // update path but not undisturbedPath
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // TODO: style rename dialog box.
            // TODO: test renaming to same folder
            // TODO: initialize dialog input box to current folder (the one to be renamed) name
            case R.id.action_rename:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Title");
                final EditText input = new EditText(this);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FileListItem toBeRenamed = getFileItemsSelected().get(0);
                        File fileToBeRenamed = new File(toBeRenamed.getFilePath() + toBeRenamed.getFileName());
                        File fileToBeRenamedTo = new File(toBeRenamed.getFilePath() + input.getText().toString());
                        boolean renameStatus = renameFile(fileToBeRenamed, fileToBeRenamedTo);

                        if (!renameStatus) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Rename failed", Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            prepareFileItemsFromPath();
                            restartListFragment();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

                return true;

            case R.id.action_delete:

                // TODO: test the case where one delete fail, one pass.
                boolean allFailed = true;
                for (int i = 0; i < getFileItemsSelected().size(); i++) {
                    FileListItem toBeDeleted = getFileItemsSelected().get(i);
                    File fileToBeDeleted = new File(toBeDeleted.getFilePath() + toBeDeleted.getFileName());
                    boolean deleteStatus = deleteFile(fileToBeDeleted);

                    if (!deleteStatus) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Delete failed", Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        allFailed = false;
                    }
                }

                if (!allFailed) {
                    prepareFileItemsFromPath();
                    restartListFragment();
                }

                return true;

            case R.id.action_refresh:
                prepareFileItemsFromPath();
                restartListFragment();

                return true;

            case R.id.action_sort:
                return true;

            // For move and copy use createNewFile/mkdir maybe? Can move multiple files, folders and a mixture of the two
            case R.id.action_move:
                ArrayList<FileListItem> selectedMoveItems = getFileItemsSelected();

                // save selected file items in the clipboard
                clipboard = selectedMoveItems;
                clipboardOperation = "move";

                // mark each selected item as unselected
                for (int i = 0; i < selectedMoveItems.size(); i++) {
                    selectedMoveItems.get(i).setIsSelected(false);
                }

                restartListFragment();
                invalidateOptionsMenu();

                // TODO Error message when trying to move root/abc to root/abc/child_abc/
                // TODO fire dialog to inform user about clipboard and paste operation.
                return true;

            case R.id.action_copy:
                ArrayList<FileListItem> selectedCopyItems = getFileItemsSelected();

                // save selected file items in the clipboard
                clipboard = selectedCopyItems;
                clipboardOperation = "copy";

                // mark each selected item as unselected
                for (int i = 0; i < selectedCopyItems.size(); i++) {
                    selectedCopyItems.get(i).setIsSelected(false);
                }

                restartListFragment();
                invalidateOptionsMenu();
                return true;

            case R.id.action_paste:
                for (int i = 0; i < clipboard.size(); i++) {

                    String parentPath = path;
                    if (!parentPath.endsWith(File.separator)) {
                        parentPath = parentPath + File.separator;
                    }

                    File clipboardFile = new File(clipboard.get(i).getFilePath() + clipboard.get(i).getFileName());
                    File targetFile = new File(parentPath + clipboard.get(i).getFileName());
                    if (clipboardOperation == "move") {

                        boolean moveStatus = renameFile(clipboardFile, targetFile);
                        if (moveStatus) {
                            prepareFileItemsFromPath();
                            restartListFragment();
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Move operation failed", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    } else {
                        boolean copyStatus = copyFilesOrDirectories(clipboardFile, targetFile);
                        if (copyStatus) {
                            prepareFileItemsFromPath();
                            restartListFragment();
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Copy operation failed", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                    clipboard.clear();
                    invalidateOptionsMenu();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // populate file items list based on path
        prepareFileItemsFromPath();

        setContentView(R.layout.activity_main);

        TabViewFragment tabFrag = new TabViewFragment();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.fragment_container, tabFrag, "tab_frag").commit();

        // Initialize fragment to display file list
        ListFileFragment frag = new ListFileFragment();
        fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.fragment_container, frag, "pho_tag").commit();

        // Initialize toolbar
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setToolbarStyles();
        setSupportActionBar(myToolbar);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < fileListItems.size(); i++) {
                    fileListItems.get(i).setIsSelected(false);
                    restartListFragment();
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogInputBox();
                }
            });
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        ArrayList<FileListItem> itemsSelected = getFileItemsSelected();
        MenuItem copy = menu.findItem(R.id.action_copy);
        copy.setVisible(false);
        MenuItem paste = menu.findItem(R.id.action_paste);
        paste.setVisible(false);
        MenuItem delete = menu.findItem(R.id.action_delete);
        delete.setVisible(false);
        MenuItem refresh = menu.findItem(R.id.action_refresh);
        refresh.setVisible(false);
        MenuItem rename = menu.findItem(R.id.action_rename);
        rename.setVisible(false);
        MenuItem move = menu.findItem(R.id.action_move);
        move.setVisible(false);
        MenuItem groupBy = menu.findItem(R.id.action_group_by);
        groupBy.setVisible(false);
        MenuItem sort = menu.findItem(R.id.action_sort);
        sort.setVisible(false);

        if (itemsSelected.size() == 1) {
            move.setVisible(true);
            delete.setVisible(true);
            copy.setVisible(true);
            rename.setVisible(true);
        } else if (itemsSelected.size() > 1) {
            move.setVisible(true);
            delete.setVisible(true);
            copy.setVisible(true);
        } else {
            groupBy.setVisible(true);
            sort.setVisible(true);
            refresh.setVisible(true);

            if (clipboard.size() > 0) {
                paste.setVisible(true);
            }
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // TODO check for the case when permissions are required: are the files loaded prematurely?
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            // fireListFilesIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission granted!
                    // TODO: fix this bug. This should be uncommented. Test the case when permissions are not granted
                    // fireListFilesIntent();
                } else {
                    // TODO: permission denied ... quit app?
                    Log.d("MainActivity", "Permission denied");
                }

                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        String base_path = Environment.getExternalStorageDirectory().toString();

        if (path == base_path) {
            // TODO error on back press. Fix this
            super.onBackPressed();
            // TODO: navigate to main view
        } else {
            String newPath = "";
            if (path.length() > base_path.length()) {
                String elements[] = path.split(File.separator);
                for (int i = 0; i < elements.length - 1; i++) {
                    if (!elements[i].isEmpty() && elements[i] != "") {
                        newPath = newPath + File.separator + path.split(File.separator)[i];
                    }
                }
            }

            onFileItemClicked(newPath);
        }
    }

    // --------- Helpers --------------

    private void prepareFileItemsFromPath() {
        // TODO: for sorting, sort fileListItems according to sort order here
        fileListItems.clear();
        String[] files = (new File(path)).list(filter);
        for(int i = 0; i < files.length; i++) {

            // parentPath is the path of the directory the file in question is in.
            String parentPath = path;
            if (!parentPath.endsWith(File.separator)) {
                parentPath = parentPath + File.separator;
            }

            FileListItem fileListItem = new FileListItem(files[i], parentPath);
            fileListItems.add(fileListItem);
        }
    }

    private boolean renameFile(File from, File to) {
        return from.exists() && !to.exists() && from.renameTo(to);
    }

    // TODO: fix delete by looping over all children and deleting one by one
    private boolean deleteFile(File file) {

        if (!file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            String[] contents = file.list();
            for (int i = 0; i < contents.length; i++) {

                // TODO error handling
                boolean a = deleteFile(new File(file.getAbsolutePath(), contents[i]));
            }
        }
        return file.delete();
    }

    public void copy(File src, File dst) throws IOException {
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

    private boolean copyFilesOrDirectories(File from, File to) {
        if (!from.exists()) {
            return false;
        }

        if (from.isDirectory()) {
            if (!to.exists()) {
                to.mkdir();
            }

            String[] files = from.list();
            // Log.d("copyFilesOrDirectories", files.length + "");
            for(int i = 0; i < files.length; i++) {

                // parentPath is the path of the directory the file in question is in.
                String parentPath = from.getAbsolutePath();
                if (!parentPath.endsWith(File.separator)) {
                    parentPath = parentPath + File.separator;
                }

                Log.d("copyFilesOrDirectories", parentPath + files[i]);
                Log.d("copyFilesOrDirectories", new File(to.getAbsolutePath(), files[i]).getAbsolutePath());
                // TODO error handling
                boolean a = copyFilesOrDirectories(new File(parentPath + files[i]), new File(to.getAbsolutePath(), files[i]));
            }

            Log.d("copyFilesOrDirectories", "directory!!!!");
            return true;
        } else {
            Log.d("copyFilesOrDirectories", "file!!!!");
            try {
                copy(from, to);
                return true;
            } catch (IOException ex) {

            }
            return false;
        }
    }

    private void setToolbarStyles() {
        int noFileItemsSelected = noFileItemsSelected();

        // TODO: based on no of items selected hide menu items appropriately

        if (noFileItemsSelected > 0) {
            myToolbar.setTitle(noFileItemsSelected + " selected");
            myToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            myToolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorToolbarSelected));
        } else {
            myToolbar.setNavigationIcon(null);

            // TODO: What should be the title when no item is selected
            myToolbar.setTitle("PhileStore");
            myToolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorToolbarDefault));
        }
    }

    private void restartListFragment() {
        setToolbarStyles();
        restartTabViewFragment();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ListFileFragment listFrag = (ListFileFragment) fm.findFragmentByTag("pho_tag");

        ft.detach(listFrag).attach(listFrag);
        ft.commit();
    }

    private void restartTabViewFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        TabViewFragment tabFrag = (TabViewFragment) fm.findFragmentByTag("tab_frag");

        ft.detach(tabFrag).attach(tabFrag);
        ft.commit();
    }

    private ArrayList<FileListItem> getFileItemsSelected() {
        ArrayList<FileListItem> fileListItemsSelected = new ArrayList<>();
        for (int i = 0; i < fileListItems.size(); i++) {
            if (fileListItems.get(i).getIsSelected()) {
                fileListItemsSelected.add(fileListItems.get(i));
            }
        }

        return fileListItemsSelected;
    }

    private int noFileItemsSelected() {
        return getFileItemsSelected().size();
    }

    private void dialogInputBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create folder");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String creationError = "";

                if (input.getText() != null && !input.getText().toString().isEmpty()) {
                    String parentPath = path;
                    if (!parentPath.endsWith(File.separator)) {
                        parentPath = parentPath + File.separator;
                    }

                    String folderName = input.getText().toString();
                    File target = new File(parentPath + folderName);

                    if (!target.exists()) {
                        boolean status = target.mkdir();

                        if (!status) {
                            creationError = "Folder creation failed";
                        }

                    } else {
                        // TODO: item already exists, do you want to replace dialog
                        creationError = "Folder already exists";
                    }
                } else {
                    creationError = "Folder name not valid";
                }

                if (creationError != "") {
                    Toast toast = Toast.makeText(getApplicationContext(), creationError, Toast.LENGTH_SHORT);
                    toast.show();
                }

                prepareFileItemsFromPath();
                restartListFragment();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }


    // --------- Getters & setters ------------------

    public ArrayList<FileListItem> getFileListItems() {
        return fileListItems;
    }

    public String getUndisturbedPath() {
        return undisturbedPath;
    }
}

// TODO contemplate using abc_ic_menu_paste_mtrl_am_alpha as paste icon. Found it in res/values/values.xml