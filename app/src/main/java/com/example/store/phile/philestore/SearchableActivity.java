package com.example.store.phile.philestore;

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

/**
 * Created by adityasridhar on 16-09-17.
 */
public class SearchableActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        Intent intent = getIntent();
        if (intent != null){
            handleIntent(intent);
        }
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            showToast("Searched for " + query);
            //use the query to search
        }
    }

    private void showToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

//    /*
//     * Inflates oveflow menu
//     */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // getMenuInflater().inflate(R.menu.menu_main, menu);
//
////        // Get the SearchView and set the searchable configuration
////        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
////        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
////        // Assumes current activity is the searchable activity
////        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
////        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
//
//        return true;
//    }

}
