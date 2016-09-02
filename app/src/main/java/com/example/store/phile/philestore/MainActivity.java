package com.example.store.phile.philestore;

import android.Manifest;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.support.v7.widget.Toolbar;

import java.io.File;
import java.util.ArrayList;

/*
  Goal:
  When no item selected, more options icon in tool bar should render normal options (Sort, About, Group by)
  When item(s) selected, more options icon in tool bar should render selected items options (Rename, Delete)

  Investigate:
    1) using a fragment/activity for more options
    2) when to exit selected items view

 */


public class MainActivity extends AppCompatActivity implements ListFileFragment.FileActionsListener {

    private String path = Environment.getExternalStorageDirectory().toString();
    private ArrayList<Integer> itemsSelected = new ArrayList<>();


    public String getFullPath() {
        return path;
    }

    public ArrayList<Integer> getItemsSelected() {
        return itemsSelected;
    }

    @Override
    public void onFileItemClicked(String p) {
        // set private path variable
        path = p;
        restartListFragment();
    }

    @Override
    public void onFileItemSelected(int pos) {
        itemsSelected.add(pos);
        restartListFragment();
    }

    private void restartListFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ListFileFragment fragment = (ListFileFragment) fm.findFragmentByTag("pho_tag");

        ft.detach(fragment).attach(fragment);
        ft.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ListFileFragment frag = new ListFileFragment();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.fragment_container, frag, "pho_tag").commit();


        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
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
            super.onBackPressed();
            // TODO: navigate to main view
        } else {
            String newPath = "";
            if (path.length() > base_path.length()) {
                for (int i = 0; i < path.split(File.separator).length - 1; i++) {
                    newPath = newPath + File.separator + path.split(File.separator)[i];
                }
            }

            onFileItemClicked(newPath);
        }

        // return true;
    }

//    private void fireListFilesIntent() {
//        Intent i = new Intent(this, ListFileFragment.class);
//        startActivity(i);
//    }
}
