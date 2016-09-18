package com.example.store.phile.philestore;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements ListFileFragment.FileActionsListener, TabViewFragment.TabActionsListener {

    private String path = Environment.getExternalStorageDirectory().toString();
    private String undisturbedPath = path;

    final private String[] sortOpts = {"Name", "Size", "Date last modified"};
    private int sortOptionIndexSelected = 0;
    boolean sortOrderIsAscending = true;


    // TODO: store sort order in bundle and restore on restart
    private Toolbar myToolbar;

    // --------- Handlers --------------

    @Override
    public void startActivity(Intent intent) {
        Log.d("MainActivity", "startActivity");
        // check if search intent
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            intent.putExtra("PATH", path);
        }

        super.startActivity(intent);
    }

    /*
     * Called when a file item is clicked
     */
    @Override
    public void onFileItemClicked(String p) {
        File f = new File(p);

        // If file clicked is a directory, set path to directory path.
        // Else if its a file, let path point to file's parent directory's path
        if (f.isDirectory()) {
            path = p;
            undisturbedPath = p;
            restartListFragment();
        } else {
            try {
                FileOpen.openFile(getApplicationContext(), f);
            } catch (IOException ex) {
                showToast("Unable to open file");
            }
        }
    }

    /*
     * Called when a file item is long pressed
     */
    @Override
    public void onFileItemSelected(int pos) {

        ListFileFragment listFrag = getListFileFragment();

        if (listFrag != null) {
            listFrag.toggleFileItemSelected(pos);
        }

        setToolbarStyles();
        invalidateOptionsMenu();
    }

    /*
     * When a tab in horizontal scroll view is selected
     */
    @Override
    public void onTabItemClicked(String itemPath) {
        // update path but not undisturbedPath
        path = itemPath;
        restartListFragment();
    }

    /*
     * Inflates oveflow menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    /*
     * Called when a menu option is selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final ListFileFragment listFrag = getListFileFragment();
        switch (item.getItemId()) {
            case R.id.action_delete:
                if (listFrag == null) {
                    return true;
                }

                listFrag.deleteFileItems();
                undisturbedPath = path;
                restartListFragment();
                setToolbarStyles();
                invalidateOptionsMenu();

                return true;

            case R.id.action_rename:
                showRenameDialog();
                return true;

            case R.id.action_refresh:
                restartListFragment();
                return true;

            case R.id.action_sort:
                AlertDialog.Builder sortDialogBldr = new AlertDialog.Builder(this);
                sortDialogBldr.setTitle(R.string.sort_title)
                    .setSingleChoiceItems(sortOpts, sortOptionIndexSelected, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sortOptionIndexSelected = which;
                        }
                    })
                    .setPositiveButton(R.string.ascending, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sortOrderIsAscending = true;
                            dialog.dismiss();
                            restartListFragment();
                        }

                    })
                    .setNegativeButton(R.string.descending, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sortOrderIsAscending = false;
                            dialog.dismiss();
                            restartListFragment();
                        }
                    });
                sortDialogBldr.show();
                return true;

            case R.id.action_move:
                listFrag.moveFileItems();
                restartListFragment();
                showToast("Saved " + listFrag.getClipboard().size() + " items to the clipboard");

                return true;

            case R.id.action_copy:
                listFrag.copyFileItems();
                restartListFragment();
                showToast("Saved " + listFrag.getClipboard().size() + " items to the clipboard");
                return true;

            case R.id.action_paste:
                listFrag.pasteFileItems();
                restartListFragment();
                return true;

            case R.id.action_search:
                // onSearchRequested();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            path = savedInstanceState.getString("PATH");
            undisturbedPath = savedInstanceState.getString("UNDISTURBED_PATH");
            sortOptionIndexSelected = savedInstanceState.getInt("SORT_OPTION");
            sortOrderIsAscending = savedInstanceState.getBoolean("SORT_IS_ASCENDING");

            // TODO: do i need to get rid of savedInstanceState?
            savedInstanceState.remove("UNDISTURBED_PATH");
            savedInstanceState.remove("PATH");
            savedInstanceState.remove("SORT_OPTION");
            savedInstanceState.remove("SORT_IS_ASCENDING");
        }

        initializeSearchPath();

        // Initialize tab fragment
        TabViewFragment tabFrag = new TabViewFragment();
        FragmentManager fm = getSupportFragmentManager();

        // If fragment was already added to the fragment container, remove it before adding it again
        if (fm.findFragmentById(R.id.tab_view_fragment_container) != null) {
            fm.beginTransaction().remove(fm.findFragmentById(R.id.tab_view_fragment_container)).commit();
        }

        fm.beginTransaction().add(R.id.tab_view_fragment_container, tabFrag, "tab_frag").commit();

        // Initialize fragment to display file list
        ListFileFragment frag = new ListFileFragment();
        fm = getSupportFragmentManager();

        // If fragment was already added to the fragment container, remove it before adding it again
        if (fm.findFragmentById(R.id.fragment_container) != null) {
            fm.beginTransaction().remove(fm.findFragmentById(R.id.fragment_container)).commit();
        }

        fm.beginTransaction().add(R.id.fragment_container, frag, "pho_tag").commit();

        setContentView(R.layout.activity_main);

        // Initialize toolbar
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setToolbarStyles();
        setSupportActionBar(myToolbar);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListFileFragment listFrag = getListFileFragment();
                listFrag.unselectAllFileItems();

                setToolbarStyles();
                invalidateOptionsMenu();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showCreateDialog();
                }
            });
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        ListFileFragment listFrag = getListFileFragment();
        int noFileItemsSelected = 0;

        if (listFrag != null) {
            noFileItemsSelected = listFrag.getNoFileItemsSelected();
        }

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
        MenuItem search = menu.findItem(R.id.action_search);
        search.setVisible(false);

        if (noFileItemsSelected == 1) {
            move.setVisible(true);
            delete.setVisible(true);
            copy.setVisible(true);
            rename.setVisible(true);
        } else if (noFileItemsSelected > 1) {
            move.setVisible(true);
            delete.setVisible(true);
            copy.setVisible(true);
        } else {
            groupBy.setVisible(true);
            sort.setVisible(true);
            refresh.setVisible(true);
            search.setVisible(true);
            if (listFrag.getClipboard().size() > 0) {
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

        if (normalizeFilePaths(path).equals(normalizeFilePaths(base_path))) {
            super.onBackPressed();
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putString("PATH", path);
        savedInstanceState.putString("UNDISTURBED_PATH", undisturbedPath);
        savedInstanceState.putInt("SORT_OPTION", sortOptionIndexSelected);
        savedInstanceState.putBoolean("SORT_IS_ASCENDING", sortOrderIsAscending);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    // --------- Helpers --------------

    private String normalizeFilePaths(String path) {
        if (!path.endsWith(File.separator)) {
            return path + File.separator;
        }

        return path;
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


    private void setToolbarStyles() {

        // Fetch no. of file items selected
        int noFileItemsSelected = 0;
        ListFileFragment listFrag = getListFileFragment();
        if (listFrag != null) {
            noFileItemsSelected = listFrag.getNoFileItemsSelected();
        }

        HorizontalScrollView horizontalScrollView = (HorizontalScrollView) findViewById(R.id.tab_view_fragment_container);

        if (horizontalScrollView == null) {
            return;
        }

        if (noFileItemsSelected > 0) {
            myToolbar.setTitle(noFileItemsSelected + " selected");
            myToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            myToolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorToolbarSelected));
            horizontalScrollView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorToolbarSelected));

        } else {
            myToolbar.setNavigationIcon(null);

            // TODO: What should be the title when no item is selected
            myToolbar.setTitle(R.string.app_name);
            myToolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorToolbarDefault));
            horizontalScrollView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorToolbarDefault));
        }
    }

    private void restartListFragment() {
        restartTabViewFragment();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ListFileFragment listFrag = (ListFileFragment) fm.findFragmentByTag("pho_tag");

        ft.detach(listFrag).attach(listFrag);
        ft.commit();

        setToolbarStyles();
        invalidateOptionsMenu();
    }

    private void restartTabViewFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        TabViewFragment tabFrag = (TabViewFragment) fm.findFragmentByTag("tab_frag");

        ft.detach(tabFrag).attach(tabFrag);
        ft.commit();
    }

    private ListFileFragment getListFileFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ListFileFragment listFrag = (ListFileFragment) fm.findFragmentByTag("pho_tag");

        return listFrag;
    }

    /*
     * Create folder dialog box
     */
    private void showCreateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create folder");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.alert_label_editor, null);
        builder.setView(view);


        final EditText input = (EditText) view.findViewById(R.id.alert_dialog_edit);

        if (input == null) {
            return;
        }

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

                // TODO: should use .equals
                if (creationError != "") {
                    showToast(creationError);
                }

                // prepareFileItemsFromPath();
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

    private void showRenameDialog() {
        final ListFileFragment listFrag = getListFileFragment();

        AlertDialog.Builder bldr = new AlertDialog.Builder(this);
        bldr.setTitle("Rename folder");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.rename_alert_dialog, null);
        bldr.setView(view);

        final EditText input = (EditText) view.findViewById(R.id.alert_dialog_edit);


        if (input == null) {
            return;
        }

        input.setText(listFrag.getFileItemsSelected().get(0).getFileName());

        bldr.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listFrag == null) {
                    return;
                }

                listFrag.renameFileItem(input.getText().toString());
                undisturbedPath = path;
                restartListFragment();
            }
        });
        bldr.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        bldr.show();
    }

    private void showToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void initializeSearchPath() {
        if (getIntent() != null && getIntent().getStringExtra("SEARCH_PATH") != null) {
            path = getIntent().getStringExtra("SEARCH_PATH");
            undisturbedPath = path;
        }
    }

    // --------- Getters & setters ------------------

    public String getUndisturbedPath() {
        return undisturbedPath;
    }

    public String getPath() {
        return path;
    }

    public int getSortOptionIndexSelected() {
        return sortOptionIndexSelected;
    }

    public boolean getSortOrderIsAscending() {
        return sortOrderIsAscending;
    }

}