package com.shadowckk.remind.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.baidulocationdemo.R;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;

/**
 * 选择地点页面：
 * 设置提醒范围
 * 选择提醒地点
 * 设置进入提醒、离开提醒
 */
public class LocationNotifyActivity extends AppCompatActivity implements BaiduMap.OnMapClickListener {

    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private EditText mRadius;
    private Button inRemind;
    private Button outRemind;

    private LocationClient mLocationClient;
    private NotiftLocationListener listener;

    private BitmapDescriptor bd = BitmapDescriptorFactory.fromResource(R.drawable.icon_openmap_mark);

    boolean isFirstLoc = true; //是否首次定位

    //当前经纬度
    private double CurrentLat;
    private double CurrentLon;

    //目的地经纬度
    private double DestinationLat = 0.0d;
    private double DestinationLon = 0.0d;
    LatLng Destination;
    String DestinationName; //目的地名称

    //选择点经纬度以及是否已经选择
    String SelectedDestinationName;
    LatLng SelectedDestination;
    boolean isSelected = false;

    //是否已到达目的地,初始为false
    boolean isArrived = false;
    double Earth_Radius = 6371.393;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fence);

        //设置工具栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //组件定位
        mMapView = findViewById(R.id.bmap);
        mRadius = findViewById(R.id.radius);
        inRemind = findViewById(R.id.in_remind);
        outRemind = findViewById(R.id.out_remind);

        //监听地图
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMapClickListener(this);

        //定位初始化
        try {
            mLocationClient = new LocationClient(this);
            //配置定位参数
            initLocationOption();
            //获取定位数据
            listener = new NotiftLocationListener();
            //开启定位图层
            mBaiduMap.setMyLocationEnabled(true);
            if (mLocationClient != null) {
                //监听定位
                mLocationClient.registerLocationListener(listener);
                mLocationClient.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //监听地图，设置目的地
        SetDestination();

        //监听进入提醒按钮
        inRemindOnClick();

        //监听离开提醒按钮
        outRemindOnClick();
    }


    //点击地图上的点实现目的地选择
    public void SetDestination() {
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            boolean isSelected;

            public void onMapClick(LatLng latLng) {
            }

            public void onMapPoiClick(MapPoi mapPoi) {
                SelectedDestinationName = mapPoi.getName();
                SelectedDestination = mapPoi.getPosition();
                //提示用户是否将该点确认为目的地
                AlertDialog.Builder adb = new AlertDialog.Builder(LocationNotifyActivity.this);
                adb.setTitle("选择目的地");
                if (!isSelected) {
                    adb.setMessage("您确定要将" + SelectedDestinationName + "设置为目的地吗?");
                } else {
                    adb.setMessage("您确定要将目的地改为" + SelectedDestinationName + "吗？");
                }
                adb.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        initDestination();
                    }
                });
                adb.setNegativeButton("取消", null);
                adb.show();
            }
        });
    }

    //角度值转弧度值
    public double rad(double d) {
        return d * Math.PI / 180;
    }

    //计算当前位置和目的地的距离
    public double distance(double lat1, double lon1, double lat2, double lon2) {
        //将经纬度转化为弧度制
        lat1 = rad(lat1);
        lat2 = rad(lat2);
        lon1 = rad(lon1);
        lon2 = rad(lon2);
        //点A：纬度角β1、经度角α1；点B：纬度角β2、经度角α2。
        //则距离S=R·arccos[cosβ1cosβ2cos(α1-α2)+sinβ1sinβ2]，其中R为球体半径。
        return 1000 * Earth_Radius * Math.acos(Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2) + Math.sin(lat1) * Math.sin(lat2));
    }

    public void initDestination() {
        isArrived = false;
        isSelected = true;
        DestinationName = SelectedDestinationName;
        Destination = SelectedDestination;
        DestinationLat = Destination.latitude;
        DestinationLon = Destination.longitude;
        //将该点在地图上标记
        mBaiduMap.clear();
        MarkerOptions ooA = new MarkerOptions()
                .position(Destination)
                .icon(bd)
                .zIndex(9)
                .draggable(true);
        Marker mk = (Marker) mBaiduMap.addOverlay(ooA);
        LatLng latLng = new LatLng(DestinationLat, DestinationLon);
        if (mRadius.getText().toString().equals("")) {
            Toast.makeText(LocationNotifyActivity.this, "请输入提醒范围", Toast.LENGTH_SHORT).show();
        } else {
            int radius = Integer.parseInt(mRadius.getText().toString());
            addCircle(latLng, radius); //添加圆形围栏
            Toast.makeText(getApplicationContext(), "您已选择" + DestinationName + "为目的地", Toast.LENGTH_SHORT).show();
        }
    }

    //配置定位参数
    public void initLocationOption() {
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

    @Override
    public void onMapClick(LatLng latLng) {
    }

    @Override
    public void onMapPoiClick(MapPoi mapPoi) {
    }

    //进入提醒
    public void inRemindOnClick() {
        inRemind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRadius.getText().toString().equals("")) {
                    Toast.makeText(LocationNotifyActivity.this, "请输入提醒范围", Toast.LENGTH_SHORT).show();
                } else {
                    int radius = Integer.parseInt(mRadius.getText().toString());
                    if (DestinationLat == 0.0d || DestinationLon == 0.0d) {
                        Toast.makeText(LocationNotifyActivity.this, "请点击地图添加中心点坐标", Toast.LENGTH_SHORT).show();
                    } else if (distance(CurrentLat, CurrentLon, DestinationLat, DestinationLon) < radius) {
                        Toast.makeText(LocationNotifyActivity.this, "您已在提醒范围内", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent2 = new Intent(LocationNotifyActivity.this, AddTaskActivity.class);
                        intent2.putExtra("type", 1);
                        intent2.putExtra("destinationLat", DestinationLat);
                        intent2.putExtra("destinationLon", DestinationLon);
                        intent2.putExtra("radius", radius);
                        intent2.putExtra("destinationName", DestinationName);
                        setResult(Activity.RESULT_OK, intent2);
                        LocationNotifyActivity.this.finish();
                    }
                }
            }
        });
    }

    //离开提醒
    public void outRemindOnClick() {
        outRemind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRadius.getText().toString().equals("")) {
                    Toast.makeText(LocationNotifyActivity.this, "请输入提醒范围", Toast.LENGTH_SHORT).show();
                } else {
                    int radius = Integer.parseInt(mRadius.getText().toString());
                    if (DestinationLat == 0.0d || DestinationLon == 0.0d) {
                        Toast.makeText(LocationNotifyActivity.this, "请点击地图添加中心点坐标", Toast.LENGTH_SHORT).show();
                    } else if (distance(CurrentLat, CurrentLon, DestinationLat, DestinationLon) > radius) {
                        Toast.makeText(LocationNotifyActivity.this, "您已在提醒范围外", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent2 = new Intent(LocationNotifyActivity.this, AddTaskActivity.class);
                        intent2.putExtra("type", 2);
                        intent2.putExtra("destinationLat", DestinationLat);
                        intent2.putExtra("destinationLon", DestinationLon);
                        intent2.putExtra("radius", radius);
                        intent2.putExtra("destinationName", DestinationName);
                        setResult(Activity.RESULT_OK, intent2);
                        LocationNotifyActivity.this.finish();
                    }
                }
            }
        });
    }

    //定位请求回调接口
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

            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                    //设置获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);
            //防止每次定位都重新设置中心点
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(CurrentLat, CurrentLon);
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
    }

    public void addCircle(LatLng latLng, int radius) {
        // 绘制圆
        OverlayOptions ooCircle = new CircleOptions()
                .fillColor(0x00000000)
                .center(latLng)
                .stroke(new Stroke(4, 0xA0008080))
                .radius(radius);
        mBaiduMap.addOverlay(ooCircle);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放资源
        bd.recycle();
        if (mLocationClient != null) {
            // 停止定位
            mLocationClient.stop();
        }
        // 释放地图资源
        mBaiduMap.clear();
        mMapView.onDestroy();
    }
}
