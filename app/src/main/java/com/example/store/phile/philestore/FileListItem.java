package com.example.store.phile.philestore;

import android.os.Parcelable;

/**
 * Created by adityasridhar on 16-09-01.
 */
public class FileListItem {
    private String fileName;
    private String filePath;
    // private Image img;

    FileListItem(String fName, String fPath) {
        fileName = fName;
        filePath = fPath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return fileName;
    }
}
