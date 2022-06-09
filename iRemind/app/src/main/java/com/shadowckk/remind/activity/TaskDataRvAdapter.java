package com.shadowckk.remind.activity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.baidulocationdemo.R;
import com.shadowckk.remind.DB.DBContruct;
import com.shadowckk.remind.DB.DBDataUtils;

import java.text.SimpleDateFormat;

public class TaskDataRvAdapter extends RecyclerView.Adapter<TaskDataRvAdapter.MyViewHolder> {
    private Context mContext;
    private Cursor mCursorTask;
    private Cursor mCursorTasked;
    private DBDataUtils dbDataUtils = new DBDataUtils();

    public TaskDataRvAdapter(Context context, Cursor cursorTask, Cursor cursorTasked) {
        mContext = context;
        mCursorTask = cursorTask;
        mCursorTasked = cursorTasked;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.member_rv_task_list, parent, false);
        return new MyViewHolder(view);
    }

    //在这个方法里面我填了Google留下的十万个坑。
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        if (position <= mCursorTask.getCount() - 1) {
            if (mCursorTask.moveToPosition(position)) {
                Log.v("lwl", "适配器：开始加载待办:" + String.valueOf(position));

                final String taskContent = mCursorTask.getString(mCursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_TASK_DATA_STRING));
                final int type = mCursorTask.getInt(mCursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_TYPE));
                final String taskTime = mCursorTask.getString(mCursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_TIME_DATA_STRING));
                //final String timestamp = mCursorTask.getString(mCursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_TIMESTAMP));
                final double locLat = mCursorTask.getDouble(mCursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_LOC_LATITUDE));
                final double locLon = mCursorTask.getDouble(mCursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_LOC_LONGITUDE));
                final int radius = mCursorTask.getInt(mCursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_LOC_RADIUS));
                final String locName = mCursorTask.getString(mCursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_LOC_NAME));
                final long taskId = mCursorTask.getLong(mCursorTask.getColumnIndex(DBContruct.TaskDataEntry._ID));

                holder.TVtaskContent.setText(taskContent);
                //holder.TVtaskTime.setText(parseTimes(taskTime));
                if (type == 0) {
                    holder.TVtaskTime.setText(parseTimes(taskTime));
                } else if (type == 1) {
                    holder.TVtaskTime.setText("进入 " + locName);
                } else {
                    holder.TVtaskTime.setText("离开 " + locName);
                }
                holder.TVtaskContent.getPaint().reset();
                holder.CBtaskFinish.setChecked(false);
                holder.TVtaskContent.setTextColor(Color.BLACK);
                holder.TVtaskContent.setTextSize(20);
                holder.itemView.setTag(R.string.view_key_id, taskId);
                holder.itemView.setTag(R.string.state_key_id, 1);//为了辨明是爱一个数据库

                holder.TVtaskTime.setTextColor(ContextCompat.getColor(mContext, R.color.defaultColor));
                //                holder.TVtaskContent.setTextColor(Color.BLACK);
                holder.CBtaskFinish.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //TODO:DELETE AND ADD REFRESH
                        Log.v("lwl", "task的cb被丶了！");
                        dbDataUtils.addDBData(mContext, taskContent, type, taskTime, locLat, locLon, radius, locName, 2);
                        dbDataUtils.delTask(mContext, taskId, 1);//1:删除task表里面的，其他数字是tasked表。
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                swapCursor();
                            }
                        });
                    }
                });
            }
        } else {
            Log.v("lwl", "适配器：开始加载完成的待办:" + String.valueOf(position - mCursorTask.getCount()));
            if (mCursorTasked.moveToPosition(position - mCursorTask.getCount())) {

                final String taskContent = mCursorTasked.getString(mCursorTasked.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_TASK_DATA_STRING));
                final int type = mCursorTasked.getInt(mCursorTasked.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_TYPE));
                final String taskTime = mCursorTasked.getString(mCursorTasked.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_TIME_DATA_STRING));
                //final String timestamp = mCursorTask.getString(mCursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_TIMESTAMP));
                final double locLat = mCursorTasked.getDouble(mCursorTasked.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_LOC_LATITUDE));
                final double locLon = mCursorTasked.getDouble(mCursorTasked.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_LOC_LONGITUDE));
                final int radius = mCursorTasked.getInt(mCursorTasked.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_LOC_RADIUS));
                final String locName = mCursorTasked.getString(mCursorTasked.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_LOC_NAME));
                final long taskId = mCursorTasked.getLong(mCursorTasked.getColumnIndex(DBContruct.TaskDataEntry._ID));

                holder.TVtaskContent.setText(taskContent);
                holder.TVtaskContent.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                //holder.TVtaskTime.setText(parseTimes(taskTime));
                if (type == 0) {
                    holder.TVtaskTime.setText(parseTimes(taskTime));
                } else if (type == 1) {
                    holder.TVtaskTime.setText("进入 " + locName);
                } else {
                    holder.TVtaskTime.setText("离开 " + locName);
                }
                holder.CBtaskFinish.setChecked(true);
                holder.TVtaskContent.setTextColor(ContextCompat.getColor(mContext, R.color.unnecessaryGrey));
                holder.TVtaskContent.setTextSize(20);
                holder.itemView.setTag(R.string.view_key_id, taskId);
                holder.itemView.setTag(R.string.state_key_id, 2);//为了辨明是爱一个数据库
                holder.TVtaskTime.setTextColor(ContextCompat.getColor(mContext, R.color.unnecessaryGrey));
                holder.CBtaskFinish.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //TODO:DELETE AND ADD REFRESH
                        Log.v("lwl", "tasked的cb被丶了！");
                        dbDataUtils.addDBData(mContext, taskContent, type, taskTime, locLat, locLon, radius, locName, 1);
                        dbDataUtils.delTask(mContext, taskId, 2);//1:删除task表里面的，其他数字是tasked表。
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                swapCursor();
                            }
                        });
                    }
                });
            }
        }


    }

    @Override
    public int getItemCount() {
        int qqqq = mCursorTask.getCount() + mCursorTasked.getCount();
        Log.v("lwl", "适配器列表总数：" + qqqq);
        return mCursorTask.getCount() + mCursorTasked.getCount();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView TVtaskContent;
        private TextView TVtaskTime;
        private CheckBox CBtaskFinish;

        public MyViewHolder(View itemView) {
            super(itemView);
            TVtaskContent = (TextView) itemView.findViewById(R.id.mem_task_content);
            TVtaskTime = (TextView) itemView.findViewById(R.id.mem_task_time);
            CBtaskFinish = (CheckBox) itemView.findViewById(R.id.cb_task_finish);

        }
    }

    //智能化规范化处理时间！！！
    private String parseTimes(String timeStr) {//可根据需要自行截取数据显示
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        try {
            return format.format(format1.parse(timeStr));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public Integer swapCursor() {
        // Always close the previous mCursor first
        if (mCursorTask != null && mCursorTasked != null) {
            mCursorTask.close();
            mCursorTasked.close();
        }
        mCursorTask = dbDataUtils.getTaskDB(mContext);
        mCursorTasked = dbDataUtils.getTaskedDB(mContext);

        if (mCursorTask != null && mCursorTasked != null) {
            notifyDataSetChanged();
        }

        return mCursorTask.getCount() + mCursorTasked.getCount();
    }


}


