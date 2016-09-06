package com.example.store.phile.philestore;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by adityasridhar on 16-09-06.
 */
public class TabViewFragment extends Fragment {

    // Need "undisturbed" path from main activity

    // when a file/folder is selected, update "undisturbed" path

    // also need current folder open


    private Activity mAct;
    public interface TabActionsListener{
        public void onTabItemClicked(String itemPath);
    }

    @Override
    public void onAttach(Context c)
    {
        super.onAttach(c);
        mAct = c instanceof Activity ? (Activity) c : null;

        Log.d("ListFileFragment", mAct == null ? "is null" : "not null");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_view_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // from mAct get path

        if (mAct != null) {
            String undisturbedPath = ((MainActivity)mAct).getUndisturbedPath();

            String[] items = undisturbedPath.split("/");

            for (int i = 0; i < items.length; i++) {
                Log.d("onActivityCreated", items[i]);
            }

            LinearLayout layout = (LinearLayout)mAct.findViewById(R.id.tab_view);
            TextView tview1 = new TextView(mAct);
            tview1.setText("abc  ");
            layout.addView(tview1);

            TextView tview2 = new TextView(mAct);
            tview2.setText("xyz  ");
            layout.addView(tview2);

            TextView tview3 = new TextView(mAct);
            tview3.setText("lmn  ");
            layout.addView(tview3);
        }

    }
}
