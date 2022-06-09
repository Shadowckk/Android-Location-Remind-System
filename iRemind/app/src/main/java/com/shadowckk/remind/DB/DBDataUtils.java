package com.shadowckk.remind.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBDataUtils {

    private SQLiteDatabase mDb;

    public Long addDBData(Context context, String taskStr, int type, String timeStr, double locLatitude, double locLongitude, int radius, String locName, int tag){

        DBHelper dbHelper = new DBHelper(context);
        mDb = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (tag == 1){
            cv.put(DBContruct.TaskDataEntry.COLUMN_TASK_DATA_STRING,taskStr);
            cv.put(DBContruct.TaskDataEntry.COLUMN_TYPE,type);
            cv.put(DBContruct.TaskDataEntry.COLUMN_TIME_DATA_STRING,timeStr);
            cv.put(DBContruct.TaskDataEntry.COLUMN_LOC_LATITUDE,locLatitude);
            cv.put(DBContruct.TaskDataEntry.COLUMN_LOC_LONGITUDE,locLongitude);
            cv.put(DBContruct.TaskDataEntry.COLUMN_LOC_RADIUS,radius);
            cv.put(DBContruct.TaskDataEntry.COLUMN_LOC_NAME,locName);
            return mDb.insert(DBContruct.TaskDataEntry.TABLE_NAME,null,cv);
        }else{
            cv.put(DBContruct.TaskedDataEntry.COLUMN_TASK_DATA_STRING,taskStr);
            cv.put(DBContruct.TaskedDataEntry.COLUMN_TYPE,type);
            cv.put(DBContruct.TaskedDataEntry.COLUMN_TIME_DATA_STRING,timeStr);
            cv.put(DBContruct.TaskedDataEntry.COLUMN_LOC_LATITUDE,locLatitude);
            cv.put(DBContruct.TaskedDataEntry.COLUMN_LOC_LONGITUDE,locLongitude);
            cv.put(DBContruct.TaskedDataEntry.COLUMN_LOC_RADIUS,radius);
            cv.put(DBContruct.TaskedDataEntry.COLUMN_LOC_NAME,locName);
            return mDb.insert(DBContruct.TaskedDataEntry.TABLE_NAME,null,cv);
        }
    }

    //zsq add
    public Cursor findById(Context context, int id){
        DBHelper dbHelper = new DBHelper(context);
        mDb = dbHelper.getReadableDatabase();
        Cursor cursor = mDb.query(
                DBContruct.TaskDataEntry.TABLE_NAME,
                null,
                "_ID = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null);
        return cursor;
    }

    public Cursor getTaskDB(Context context){

        DBHelper dbHelper = new DBHelper(context);
        mDb = dbHelper.getWritableDatabase();
        Cursor cursor = mDb.query(
                DBContruct.TaskDataEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DBContruct.TaskDataEntry.COLUMN_TIMESTAMP+" desc");
//        mDb.close();//关闭数据库
        return cursor;
    }

    public Cursor getTaskedDB(Context context){

        DBHelper dbHelper = new DBHelper(context);
        mDb = dbHelper.getWritableDatabase();
        Cursor cursor = mDb.query(
                DBContruct.TaskedDataEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DBContruct.TaskedDataEntry.COLUMN_TIMESTAMP+" desc");
//        mDb.close();//关闭数据库
        return cursor;
    }

    public boolean delTask(Context context, long id, int tag){

        DBHelper dbHelper = new DBHelper(context);
        mDb = dbHelper.getWritableDatabase();
        if (tag == 1)
            return mDb.delete(DBContruct.TaskDataEntry.TABLE_NAME,
                    DBContruct.TaskDataEntry._ID + "=" + id,null) > 0;
        else
            return mDb.delete(DBContruct.TaskedDataEntry.TABLE_NAME,
                    DBContruct.TaskedDataEntry._ID + "=" + id,null) > 0;

    }

    public void closeDB(){
        mDb.close();
    }
}
