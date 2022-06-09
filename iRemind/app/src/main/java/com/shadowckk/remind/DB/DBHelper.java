package com.shadowckk.remind.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2018/7/10.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TaskData.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //sqLiteDatabase.execSQL("DROP TABLE TaskData.taskdata");
        final String SQL_CREATE_TASK_DATA_TABLE = "CREATE TABLE "+ DBContruct.TaskDataEntry.TABLE_NAME
                +" ("
                + DBContruct.TaskDataEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DBContruct.TaskDataEntry.COLUMN_TASK_DATA_STRING+ " TEXT NOT NULL, "
                + DBContruct.TaskDataEntry.COLUMN_TYPE+ " INTEGER DEFAULT -1, "
                + DBContruct.TaskDataEntry.COLUMN_TIME_DATA_STRING+" TEXT, "
                + DBContruct.TaskDataEntry.COLUMN_TIMESTAMP+" TIME, "
                + DBContruct.TaskDataEntry.COLUMN_LOC_LATITUDE+ " REAL DEFAULT 0.0, "
                + DBContruct.TaskDataEntry.COLUMN_LOC_LONGITUDE+ " REAL DEFAULT 0.0, "
                + DBContruct.TaskDataEntry.COLUMN_LOC_RADIUS+ " INTEGER DEFAULT 0, "
                + DBContruct.TaskDataEntry.COLUMN_LOC_NAME+ " TEXT"
                +");";
        final String SQL_CREATE_TASKED_DATA_TABLE = "CREATE TABLE IF NOT EXISTS "+ DBContruct.TaskedDataEntry.TABLE_NAME
                +" ("
                + DBContruct.TaskDataEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DBContruct.TaskDataEntry.COLUMN_TASK_DATA_STRING+ " TEXT NOT NULL, "
                + DBContruct.TaskDataEntry.COLUMN_TYPE+ " INTEGER DEFAULT -1, "
                + DBContruct.TaskDataEntry.COLUMN_TIME_DATA_STRING+" TEXT, "
                + DBContruct.TaskDataEntry.COLUMN_TIMESTAMP+" TIME, "
                + DBContruct.TaskDataEntry.COLUMN_LOC_LATITUDE+ " REAL DEFAULT 0.0, "
                + DBContruct.TaskDataEntry.COLUMN_LOC_LONGITUDE+ " REAL DEFAULT 0.0, "
                + DBContruct.TaskDataEntry.COLUMN_LOC_RADIUS+ " INTEGER DEFAULT 0, "
                + DBContruct.TaskDataEntry.COLUMN_LOC_NAME+ " TEXT"
                +");";
        sqLiteDatabase.execSQL(SQL_CREATE_TASK_DATA_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TASKED_DATA_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ DBContruct.TaskDataEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ DBContruct.TaskedDataEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
