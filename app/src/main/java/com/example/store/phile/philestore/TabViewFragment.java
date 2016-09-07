package com.example.store.phile.philestore;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.TextViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

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

    private String normalizePaths(String path) {
        if (!path.endsWith(File.separator)) {
            return path + File.separator;
        }

        return path;
    }

    private int getTextStyle(String filePath) {
        String path = ((MainActivity)mAct).getPath();

        int textStyle = R.style.tabNormal;
        if (normalizePaths(filePath).equals(normalizePaths(path))) {
            textStyle = R.style.tabBold;
        }

        return textStyle;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Failsafe
        if (mAct == null) {
            return;
        }

        String undisturbedPath = ((MainActivity)mAct).getUndisturbedPath();
        final String localStoragePath = Environment.getExternalStorageDirectory().toString();

        // Failsafe
        if (!undisturbedPath.contains(localStoragePath)) {
            return;
        }

        // TODO set background of horizontal scroll view
//        if (mAct.noItemsSelected() > 0) {
//
//        }

        String tabPath = undisturbedPath.replace(localStoragePath, "");
        String[] items = tabPath.split(File.separator);

        LinearLayout layout = (LinearLayout)mAct.findViewById(R.id.tab_view);
        TextView tview = new TextView(mAct);

        tview.setText(R.string.local_storage);
        TextViewCompat.setTextAppearance(tview, getTextStyle(localStoragePath));
        tview.setAllCaps(true);
        tview.setPadding(100, 0, 0, 0);
        layout.addView(tview);
        tview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ((TabActionsListener) mAct).onTabItemClicked(localStoragePath);
                } catch (ClassCastException cce) {

                }
            }
        });

        final ArrayList<String> filePaths = new ArrayList<String>();
        filePaths.add(Environment.getExternalStorageDirectory().toString() + File.separator);

        for (int i = 0; i < items.length; i++) {
            if (!items[i].isEmpty()) {
                String filePath = filePaths.get(filePaths.size() - 1) + items[i] + File.separator;
                filePaths.add(filePath);
                ImageView iView = new ImageView(mAct);

                // TODO: copy drawable resource
                iView.setImageResource(R.drawable.abc_ic_go_search_api_mtrl_alpha);
                iView.setPadding(20, 0, 20, 0);
                layout.addView(iView);

                final TextView tview1 = new TextView(mAct);
                tview1.setText(items[i]);
                TextViewCompat.setTextAppearance(tview1, getTextStyle(filePath));
                tview1.setAllCaps(true);
                final int index = i;
                tview1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (index + 1 <= filePaths.size()) {
                            try {
                                ((TabActionsListener) mAct).onTabItemClicked(filePaths.get(index));
                            } catch (ClassCastException cce) {

                            }
                        }
                    }
                });
                layout.addView(tview1);
            }
        }
    }
}
