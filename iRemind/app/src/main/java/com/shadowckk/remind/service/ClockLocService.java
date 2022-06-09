package com.shadowckk.remind.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.shadowckk.remind.DB.DBContruct;
import com.shadowckk.remind.DB.DBDataUtils;
import com.shadowckk.remind.activity.ClockActivity;
import com.shadowckk.remind.util.WakeLockUtil;

import java.io.IOException;

/**
 * Service和Broadcast都行，此处选一个，service存活率更高
 */
public class ClockLocService extends Service {
    private static final String TAG = "ClockLocService";
    public static final String EXTRA_EVENT_ID = "extra.event.id";
    public static final String EXTRA_EVENT_REMIND_TIME = "extra.event.remind.time";
    public static final String EXTRA_EVENT = "extra.event";

    private LocationClient mLocationClient = null;
    private NotiftLocationListener listener;
    private NotifyListener mNotifyListener;

    //当前经纬度
    private double CurrentLat;
    private double CurrentLon;

    //目的地经纬度
    private double DestinationLat;
    private double DestinationLon;
    LatLng Destination;
    private int DestRadius;
    private String DestinationName;//目的地名称
    private int type;
    private String typeStr;

    private Intent intent;
    private String taskContent;

    //private Vibrator mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);

    public ClockLocService() {
        Log.d(TAG, "ClockService: Constructor");
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Toast.makeText(ClockLocService.this, "ClockLocService onBind...", Toast.LENGTH_LONG).show();
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        //Toast.makeText(ClockLocService.this, "ClockLocService onCreate...", Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        intent = i;
        WakeLockUtil.wakeUpAndUnlock();

        //声明LocationClient类
        try {
            mLocationClient = new LocationClient(super.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        initLocationOption();

        //注册LocationListener监听器
        mNotifyListener = new NotifyListener();
        listener = new NotiftLocationListener();
        if (mLocationClient != null) {
            mLocationClient.registerLocationListener(listener);
            mLocationClient.registerNotify(mNotifyListener);
        }
        //开始定位
        mLocationClient.start();

        DBDataUtils dbDataUtils = new DBDataUtils();
        Cursor cursorTask = dbDataUtils.findById(this, intent.getIntExtra("taskId", -1));
        if (cursorTask.getCount() != 0) {
            cursorTask.moveToFirst();
            taskContent = cursorTask.getString(cursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_TASK_DATA_STRING));
            DestinationLat = cursorTask.getDouble(cursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_LOC_LATITUDE));
            DestinationLon = cursorTask.getDouble(cursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_LOC_LONGITUDE));
            DestinationName = cursorTask.getString(cursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_LOC_NAME));
            DestRadius = cursorTask.getInt(cursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_LOC_RADIUS));
            type = cursorTask.getInt(cursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_TYPE));
            typeStr = (type == 1) ? "进入" : "离开";
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private Handler notifyHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mLocationClient != null) {
                mNotifyListener.SetNotifyLocation(DestinationLat, DestinationLon, DestRadius, mLocationClient.getLocOption().getCoorType());//4个参数代表要位置提醒的点的坐标，具体含义依次为：纬度，经度，距离范围，坐标系类型(gcj02,gps,bd09,bd09ll)
            }
        }
    };

    public class NotifyListener extends BDNotifyListener {
        public void onNotify(BDLocation mlocation, float distance) {
            //已到达设置监听位置附近

            //mVibrator.vibrate(1000);//振动提醒已到设定位置附近
            //Toast.makeText(ClockLocService.this, "震动提醒", Toast.LENGTH_SHORT).show();
            stopSelf();
            Intent i = new Intent();
            i.setClass(getApplicationContext(), ClockActivity.class);
            i.putExtra("taskContent", taskContent);
            i.putExtra("taskLoc", DestinationName);
            i.putExtra("type", type);
            //i.putExtra(EXTRA_EVENT_REMIND_TIME, intent.getStringExtra(EXTRA_EVENT_REMIND_TIME));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(i);
        }
    }

    public class NotiftLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //获取定位结果
            location.getTime();    //获取定位时间
            location.getLocationID();    //获取定位唯一ID，v7.2版本新增，用于排查定位问题
            location.getLocType();    //获取定位类型
            location.getLatitude();    //获取纬度信息
            location.getLongitude();    //获取经度信息
            location.getRadius();    //获取定位精准度
            location.getAddrStr();    //获取地址信息
            location.getCountry();    //获取国家信息
            location.getCountryCode();    //获取国家码
            location.getCity();    //获取城市信息
            location.getCityCode();    //获取城市码
            location.getDistrict();    //获取区县信息
            location.getStreet();    //获取街道信息
            location.getStreetNumber();    //获取街道码
            location.getLocationDescribe();    //获取当前位置描述信息
            location.getPoiList();    //获取当前位置周边POI信息

            location.getBuildingID();    //室内精准定位下，获取楼宇ID
            location.getBuildingName();    //室内精准定位下，获取楼宇名称
            location.getFloor();    //室内精准定位下，获取当前位置所处的楼层信息
            //获取当前经纬度
            CurrentLat = location.getLatitude();
            CurrentLon = location.getLongitude();

            notifyHandler.sendEmptyMessage(0);
        }
    }

    //配置定位参数
    private void initLocationOption() {
        LocationClientOption option = new LocationClientOption();
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认gcj02，设置返回的定位结果坐标系
        option.setCoorType("bd09ll");
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        int span = 1500;
        option.setScanSpan(span);//每1.5秒定位一次
        //可选，设置是否需要地址信息，默认不需要
        option.setIsNeedAddress(true);
        //可选，默认false,设置是否使用gps
        option.setOpenGps(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setLocationNotify(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIsNeedLocationPoiList(true);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIgnoreKillProcess(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        option.setEnableSimulateGps(false);

        option.setOpenAutoNotifyMode();


        //设置locationClientOption
        mLocationClient.setLocOption(option);

    }

    private void postToClockActivity(Context context, Intent intent) {
        Intent i = new Intent();
        i.setClass(context, ClockActivity.class);
        i.putExtra(EXTRA_EVENT_ID, intent.getIntExtra(EXTRA_EVENT_ID, -1));
        //Event event = mEventDao.findById(intent.getIntExtra(EXTRA_EVENT_ID, -1));
        DBDataUtils dbDataUtils = new DBDataUtils();
        //Toast.makeText(ClockLocService.this, "taskId=" + intent.getIntExtra("taskId", -1), Toast.LENGTH_LONG).show();
        Cursor cursorTask = dbDataUtils.findById(this, intent.getIntExtra("taskId", -1));
        if (cursorTask.getCount() != 0) {
            cursorTask.moveToFirst();
            String taskContent = cursorTask.getString(cursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_TASK_DATA_STRING));
            String taskTime = cursorTask.getString(cursorTask.getColumnIndex(DBContruct.TaskDataEntry.COLUMN_TIME_DATA_STRING));
            //Toast.makeText(ClockLocService.this, "Content=" + taskContent + " Time=" + taskTime, Toast.LENGTH_LONG).show();
            i.putExtra("taskContent", taskContent);
            i.putExtra("taskTime", taskTime);
            //i.putExtra(EXTRA_EVENT_REMIND_TIME, intent.getStringExtra(EXTRA_EVENT_REMIND_TIME));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }


    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        //Toast.makeText(ClockLocService.this, "ClockLocService onDestroy", Toast.LENGTH_SHORT).show();
        super.onDestroy();
        if (mLocationClient != null) {
            mLocationClient.removeNotifyEvent(mNotifyListener);
            mLocationClient.unRegisterLocationListener(listener);
            mLocationClient.stop();
        }
    }
}
