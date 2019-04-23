package com.blank.writotext.utils;

import android.provider.BaseColumns;

public class FileContract {

    private FileContract() {
    }

    public static final class FileEntry implements BaseColumns {
        public static final String TABLE_NAME = "Files";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IMG = "img";
        public static final String COLUMN_PATH = "path";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}