package com.example.store.phile.philestore;

import java.io.File;
import java.util.Comparator;

/**
 * Created by adityasridhar on 16-09-01.
 */
public class FileListItem {
    private String fileName;
    private String filePath;
    private boolean isSelected;
    private long rawSize;

    FileListItem(String fName, String fPath, long rSize) {
        fileName = fName;
        filePath = fPath;
        isSelected = false;
        rawSize = rSize;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFullPath() {
        String fPath = filePath;
        if (!fPath.endsWith(File.separator)) {
            fPath = fPath + File.separator;
        }

        return fPath + fileName;
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean s) {
        isSelected = s;
    }

    public long getRawSize() {
        return rawSize;
    }

    @Override
    public String toString() {
        return fileName;
    }

    public static Comparator<FileListItem> FileRawSizeComparatorAsc
            = new Comparator<FileListItem>() {

        public int compare(FileListItem item1, FileListItem item2) {
            long rawSize1 = item1.getRawSize();
            long rawSize2 = item2.getRawSize();

            return (int)(rawSize1 - rawSize2);
        }
    };

    public static Comparator<FileListItem> FileRawSizeComparatorDesc
            = new Comparator<FileListItem>() {

        public int compare(FileListItem item1, FileListItem item2) {
            long rawSize1 = item1.getRawSize();
            long rawSize2 = item2.getRawSize();

            return (int)(rawSize2 - rawSize1);
        }
    };

    public static Comparator<FileListItem> FileNameComparatorAsc
            = new Comparator<FileListItem>() {

        public int compare(FileListItem item1, FileListItem item2) {

            String name1 = item1.getFileName();
            String name2 = item2.getFileName();

            return name1.compareTo(name2);
        }
    };

    public static Comparator<FileListItem> FileNameComparatorDesc
            = new Comparator<FileListItem>() {

        public int compare(FileListItem item1, FileListItem item2) {

            String name1 = item1.getFileName();
            String name2 = item2.getFileName();

            return name2.compareTo(name1);
        }
    };
}