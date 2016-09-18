package com.example.store.phile.philestore;

/**
 * Created by adityasridhar on 16-09-18.
 */
public class SearchResultItem {
    private String fileName;
    private String pathName;

    SearchResultItem(String f, String p) {
        fileName = f;
        pathName = p;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPathName() {
        return pathName;
    }
}
