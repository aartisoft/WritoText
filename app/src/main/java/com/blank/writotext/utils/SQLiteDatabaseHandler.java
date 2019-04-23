package com.blank.writotext.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.blank.writotext.utils.FileContract.*;

import java.io.File;

public class SQLiteDatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FilesDB";
    public SQLiteDatabaseHandler(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FILELIST_TABLE = "CREATE TABLE " +
                FileEntry.TABLE_NAME + " (" +
                FileEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FileEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                FileEntry.COLUMN_IMG+ " INTEGER NOT NULL, " +
                FileEntry.COLUMN_PATH + " TEXT NOT NULL, " +
                FileEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";
        db.execSQL(SQL_CREATE_FILELIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + FileEntry.TABLE_NAME);
        onCreate(db);

    }

    public void addFile(Fyle file) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FileEntry.COLUMN_NAME, file.getFileName());
        values.put(FileEntry.COLUMN_PATH, file.getFilePath());
        values.put(FileEntry.COLUMN_IMG, file.getFileImg());
        // insert
        db.insert(FileEntry.TABLE_NAME,null, values);
        db.close();
    }

}
