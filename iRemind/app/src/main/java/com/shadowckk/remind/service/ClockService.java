package com.shadowckk.remind.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.shadowckk.remind.DB.DBContruct;
import com.shadowckk.remind.activity.ClockActivity;
import com.shadowckk.remind.util.WakeLockUtil;
import com.shadowckk.remind.DB.DBDataUtils;

/**
 * Service和Broadcast都行，此处选一个，service存活率更高
 */
public class ClockService extends Service {
    private static final String TAG = "ClockService";
    public static final String EXTRA_EVENT_ID = "extra.event.id";
    public static final String EXTRA_EVENT_REMIND_TIME = "extra.event.remind.time";
    public static final String EXTRA_EVENT = "extra.event";
    //private EventDao mEventDao = EventDao.getInstance();
    public ClockService() {
        Log.d(TAG, "ClockService: Constructor");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(ClockService.this, "onStartCommand,sid="+startId, Toast.LENGTH_LONG).show();
        Log.d(TAG, "onStartCommand: onStartCommand");
        WakeLockUtil.wakeUpAndUnlock();
        postToClockActivity(getApplicationContext(), intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void postToClockActivity(Context context, Intent intent) {
        Intent i = new Intent();
        i.setClass(context, ClockActivity.class);
        DBDataUtils dbDataUtils = new DBDataUtils();
        //Toast.makeText(ClockService.this, "taskId="+intent.getIntExtra("taskId", -1), Toast.LENGTH_LONG).show();
        Cursor cursorTask = dbDataUtils.findById(this, intent.getIntExtra("taskId", -1));
        if (cursorTask.getCount() != 0){
            cursorTask.moveToFirst();
            String taskContent = cursorTask.getString(cursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_TASK_DATA_STRING));
            String taskTime = cursorTask.getString(cursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_TIME_DATA_STRING));
            //Toast.makeText(ClockService.this, "Content="+taskContent+" Time="+taskTime, Toast.LENGTH_LONG).show();
            i.putExtra("taskContent", taskContent);
            i.putExtra("taskTime", taskTime);
            i.putExtra("type", 0);
            //i.putExtra(EXTRA_EVENT_REMIND_TIME, intent.getStringExtra(EXTRA_EVENT_REMIND_TIME));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }
}


