package com.example.store.phile.philestore;

/**
 * Created by adityasridhar on 16-09-06.
 */
public class TabItem {
    private String filePath;
    private String name;

    public void TabItem(String f, String n) {
        filePath = f;
        name = n;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getName() {
        return name;
    }
}
