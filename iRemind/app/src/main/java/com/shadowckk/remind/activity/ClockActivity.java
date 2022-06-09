package com.shadowckk.remind.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.baidu.baidulocationdemo.R;
import com.shadowckk.remind.util.AlertDialogUtil;

public class ClockActivity extends AppCompatActivity {
    private static final String TAG = "ClockActivity";
    public static final String EXTRA_CLOCK_EVENT = "clock.event";
    //闹铃
    private MediaPlayer mediaPlayer;
    //震动
    private Vibrator mVibrator;
    //    private EventManager mEventManger = EventManager.getInstance();
//    private Event event;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);

        //设置工具栏
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initData();
    }


    private void clock() {
        mediaPlayer.start();
        long[] pattern = new long[]{1500, 1000};
        mVibrator.vibrate(pattern, 0);
        //获取自定义布局
        View inflate = LayoutInflater.from(this).inflate(R.layout.dialog_alarm_layout, null);
        TextView textView = inflate.findViewById(R.id.tv_event);
        TextView textViewTL = inflate.findViewById(R.id.tv_time_loc);
        String taskContent = intent.getStringExtra("taskContent");
        String taskTime = intent.getStringExtra("taskTime");
        String taskLoc = intent.getStringExtra("taskLoc");
        int taskType = intent.getIntExtra("type", 0);
        //String str = (taskType == 0) ? ("现在是XXX"+taskTime) :()
        String str;
        if (taskType == 0) str = "现在是" + taskTime;
        else if (taskType == 1) str = "您已进入" + taskLoc;
        else str = "您已离开" + taskLoc;
        //textViewTL.setText(str + "，以下是您的提醒内容：");
        textViewTL.setText(str);
        textView.setText(taskContent);
        //textView.setText(String.format(getString(R.string.clock_event_msg_template), taskContent));
        Button btnConfirm = inflate.findViewById(R.id.btn_confirm);
        final AlertDialog alertDialog = AlertDialogUtil.showDialog(this, inflate);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mVibrator.cancel();
                alertDialog.dismiss();
                finish();
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        clock();
    }

    protected void initData() {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.clock);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        intent = getIntent();
//        event = getIntent().getParcelableExtra(ClockService.EXTRA_EVENT);
//        if (event == null) {
//            finish();
//        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        clock();
    }

}
