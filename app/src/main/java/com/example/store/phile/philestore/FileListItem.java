package com.example.store.phile.philestore;

/**
 * Created by adityasridhar on 16-09-01.
 */
public class FileListItem {
    private String fileName;
    private String filePath;
    private boolean isSelected;

    FileListItem(String fName, String fPath) {
        fileName = fName;
        filePath = fPath;
        isSelected = false;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean s) {
        isSelected = s;
    }

    @Override
    public String toString() {
        return fileName;
    }
}
