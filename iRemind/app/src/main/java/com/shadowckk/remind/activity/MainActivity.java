package com.shadowckk.remind.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.baidu.baidulocationdemo.R;
import com.shadowckk.remind.DB.DBDataUtils;
import com.baidu.location.LocationClient;
import com.shadowckk.remind.service.ClockManager;
import com.shadowckk.remind.service.LocationService;
import com.shadowckk.remind.service.Utils;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 主页面：
 * 权限管理
 * 待办列表管理
 * 提醒服务
 */
public class MainActivity extends AppCompatActivity {
    private final int SDK_PERMISSION_REQUEST = 127;
    private final int DIALOG_KEY_BACK = 1;
    private final int DIALOG_PERMISSION = 2;

    private FloatingActionButton fab;
    private TextView mEmptyRvList;
    private String permissionInfo;

    RecyclerView recyclerView;
    TaskDataRvAdapter myAdapter;
    private DBDataUtils dbDataUtils = new DBDataUtils();
    private ClockManager mClockManager = ClockManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_list);
        mEmptyRvList = (TextView) findViewById(R.id.empty_rv_list);

        //设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //初始化SDK
        initSDK(true);

        //获取权限
        getPersimmions();

        //初始化代办
        isFirst();
        initData();

        //监听HOME键
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeAndLockReceiver, filter);

        //监听代办右滑
        itemSwipedListener();

        //监听添加提醒按钮
        addButtonListener();

        //提醒服务
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSDK(true);
    }

    @Override
    protected void onDestroy() {
        //Toast.makeText(MainActivity.this, "onDestroy......", Toast.LENGTH_LONG).show();
        super.onDestroy();
        unregisterReceiver(mHomeAndLockReceiver);
    }

    public void addButtonListener() {
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    public void itemSwipedListener() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                long id = (long) viewHolder.itemView.getTag(R.string.view_key_id);
                int stateToken = (int) viewHolder.itemView.getTag(R.string.state_key_id);
                Log.v("TAG", " data from ");
                if (stateToken == 1) {
                    dbDataUtils.delTask(MainActivity.this, id, 1);
                } else {
                    dbDataUtils.delTask(MainActivity.this, id, 2);
                }
                int itemCount = myAdapter.swapCursor(); //刷新cursor
                if (itemCount == 0) {
                    mEmptyRvList.setVisibility(View.VISIBLE);
                }
            }
        }).attachToRecyclerView(recyclerView);
    }

    //意图跳转刷新cursor
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            int itemCount = myAdapter.swapCursor(); //刷新cursor
            if (itemCount == 0) {
                mEmptyRvList.setVisibility(View.VISIBLE);
            }
        }
    }

    public void initData() {
        Cursor cursorTask = dbDataUtils.getTaskDB(this);
        Cursor cursorTasked = dbDataUtils.getTaskedDB(this);
        if (cursorTask.getCount() == 0 && cursorTasked.getCount() == 0) {
            mEmptyRvList.setVisibility(View.VISIBLE);
        } else {
            mEmptyRvList.setVisibility(View.INVISIBLE);
        }
        recyclerView = (RecyclerView) findViewById(R.id.rv_task_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        myAdapter = new TaskDataRvAdapter(recyclerView.getContext(), cursorTask, cursorTasked);
        recyclerView.setAdapter(myAdapter);
    }

    public void isFirst() {
        SharedPreferences.Editor editor = this.getSharedPreferences("spfs", Context.MODE_PRIVATE).edit();
        SharedPreferences spfs = this.getSharedPreferences("spfs", Context.MODE_PRIVATE);
        if (spfs.getInt("firstuse", 1) == 1) {
            editor.putInt("firstuse", 0);
            editor.apply();
            // 获取当前时间
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
            Date date = new Date(System.currentTimeMillis());
            //初始化待办提醒
            dbDataUtils.addDBData(this, "作者：Shadowckk", 0, simpleDateFormat.format(date), 0, 0, 0, null, 1);
            dbDataUtils.addDBData(this, "右滑可删除待办提醒", 0, simpleDateFormat.format(date), 0, 0, 0, null, 1);
            dbDataUtils.addDBData(this, "右下方按钮可添加待办提醒", 0, simpleDateFormat.format(date), 0, 0, 0, null, 1);
        }
    }


    private void initSDK(boolean status) {
        LocationClient.setAgreePrivacy(status);
        SDKInitializer.setAgreePrivacy(getApplicationContext(), status);
        ((LocationApplication) getApplication()).locationService = new LocationService(getApplicationContext());
        try {
            SDKInitializer.initialize(getApplicationContext());
            SDKInitializer.setCoordType(CoordType.BD09LL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();

            // 定位精确位置（定位权限为必须权限，用户如果禁止，则每次进入都会申请）
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }

            // 读写权限（读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次）
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(permission)) {
                return true;
            } else {
                permissionsList.add(permission);
                return false;
            }
        } else {
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!Utils.contains(this, Utils.SP_KEY_BACK_RETURN)) {
                Utils.putString(this, Utils.SP_KEY_BACK_RETURN, Utils.SP_KEY_BACK_RETURN);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    //监听是否点击了home键将客户端推到后台
    private BroadcastReceiver mHomeAndLockReceiver = new BroadcastReceiver() {
        String SYSTEM_REASON = "reason";
        String SYSTEM_HOME_KEY = "homekey";
        String SYSTEM_HOME_RECENT = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY) || TextUtils.equals(reason, SYSTEM_HOME_RECENT)) {
                    if (!Utils.contains(MainActivity.this, Utils.SP_HOME_BACK_RETURN)) {
                        Utils.putString(MainActivity.this, Utils.SP_HOME_BACK_RETURN, Utils.SP_HOME_BACK_RETURN);
                        Toast.makeText(context, context.getString(R.string.action_background), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    };
}
