package com.shadowckk.remind.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.baidu.baidulocationdemo.R;
import com.shadowckk.remind.service.ClockLocService;
import com.shadowckk.remind.service.ClockManager;
import com.shadowckk.remind.service.ClockService;
import com.bigkoo.pickerview.TimePickerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shadowckk.remind.DB.DBDataUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 添加提醒页面：
 * 监听选择时间按钮
 * 监听选择地点按钮（意图跳转、数据回传）
 * 监听创建任务按钮（添加数据库、开启提醒服务）
 */
public class AddTaskActivity extends AppCompatActivity {

    private String taskContent;
    private int type = 0; //0-时间  1-进入  2-离开
    private String taskTimeStr;
    private double destLatitude;
    private double destLongitude;
    private int destRadius;
    private String destinationName;

    private Button taskTime;
    private Button taskLoc;
    private EditText taskContentET;
    private Date taskDate;

    private ClockManager mClockManager = ClockManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        //设置工具栏
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //检查SDK版本
        checkWP();

        //代办提醒输入框
        taskContentET = (EditText) findViewById(R.id.task);
        taskContentET.setFocusable(true);
        taskContentET.setFocusableInTouchMode(true);
        taskContentET.requestFocus();

        //监听选择时间按钮、选择地点按钮、创建任务按钮
        taskTimeButtonListener();
        taskLocButtonListener();
        taskCreateButtonListener();

    }

    private void taskCreateButtonListener() {
        FloatingActionButton taskCreate = (FloatingActionButton) findViewById(R.id.task_create);
        taskCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskContent = taskContentET.getText().toString();
                if (taskContent.equals("")) {
                    Toast.makeText(AddTaskActivity.this, "请输入内容", Toast.LENGTH_LONG).show();
                } else if (taskTimeStr == null && destinationName.equals("")) {
                    Toast.makeText(AddTaskActivity.this, "请选择时间或地点", Toast.LENGTH_LONG).show();
                }
                //添加数据库、提醒服务
                else {
                    DBDataUtils dbDataUtils = new DBDataUtils();
                    if (type == 0) {
                        long id = dbDataUtils.addDBData(AddTaskActivity.this, taskContent, type, taskTimeStr, 0, 0, 0, null, 1);
                        dbDataUtils.closeDB();
                        mClockManager.addAlarmTime(buildIntent((int) id), taskDate);

                    } else {
                        long id = dbDataUtils.addDBData(AddTaskActivity.this, taskContent, type, null, destLatitude, destLongitude, destRadius, destinationName, 1);
                        dbDataUtils.closeDB();
                        buildIntentLoc((int) id);
                    }
                    Intent intent2 = new Intent(AddTaskActivity.this, MainActivity.class);
                    setResult(Activity.RESULT_OK, intent2);
                    AddTaskActivity.this.finish();
                }
            }
        });
    }

    private void taskLocButtonListener() {
        taskLoc = (Button) findViewById(R.id.task_loc);
        taskLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddTaskActivity.this, LocationNotifyActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    private void taskTimeButtonListener() {
        taskTime = (Button) findViewById(R.id.task_time);
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(Calendar.SECOND, 0);
        final TimePickerView pvTime;
        pvTime = new TimePickerView.Builder(this, new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                taskDate = date;
                taskTimeStr = getTimes(date);
                SimpleDateFormat format2 = new SimpleDateFormat("MM-dd HH:mm");
                taskTime.setText("选择时间: " + format2.format(date));
                taskLoc.setText("选择地点");
            }
        })
                .setType(new boolean[]{false, true, true, true, true, false})
                .setLabel("年", "月", "日", "时", "", "")
                .isCenterLabel(true)
                .setDividerColor(Color.DKGRAY)
                .setContentSize(21)
                .setDate(selectedDate)
                .setDecorView(null)
                .build();
        taskTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pvTime.show(view);
                type = 0;
            }
        });
    }

    //选择地点意图跳转接收数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            type = data.getIntExtra("type", 1);
            destLatitude = data.getDoubleExtra("destinationLat", 0.0d);
            destLongitude = data.getDoubleExtra("destinationLon", 0.0d);
            destRadius = data.getIntExtra("radius", 0);
            String typeStr = (type == 1) ? "进入" : "离开";
            destinationName = data.getStringExtra("destinationName");
            taskTime.setText("选择时间");
            taskLoc.setText("选择地点: " + typeStr + " " + destinationName);
        }
    }

    private String getTimes(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    public boolean checkWP() {
        if (Build.VERSION.SDK_INT >= 23) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private PendingIntent buildIntent(int id) {
        Intent intent = new Intent();
        intent.putExtra("taskId", id);
        intent.setClass(this, ClockService.class);
        Log.d("AddTaskActivity", "buildIntent"+id);//zsq
        return PendingIntent.getService(this, 0x001, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void buildIntentLoc(int id) {
        //Toast.makeText(AddTaskActivity.this, "buildLocIntent,id="+id, Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.putExtra("taskId", id);
        intent.setClass(this, ClockLocService.class);
        Log.d("AddTaskActivity", "buildIntent"+id);//zsq
        //startActivity(intent);
        startService(intent);
        //Toast.makeText(AddTaskActivity.this, "buildLocIntent2....."+id, Toast.LENGTH_LONG).show();
        //return;
    }

}