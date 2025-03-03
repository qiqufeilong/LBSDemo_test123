package com.example.lbsdemo_test;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.clusterutil.clustering.Cluster;
import com.baidu.mapapi.clusterutil.clustering.ClusterManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public MapView mMapView = null;
    public BaiduMap mBaiduMap = null;
    public LocationClient mLocationClient = null;
    private ClusterManager<MyItem> mClusterManager;


    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 检查并请求定位权限..........
        if (checkPermissions()) {
            try {
                initMap();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            requestPermissions();
        }
        // 创建点聚合管理器实例，MyItem是自定义的聚合项类
        mClusterManager = new ClusterManager<MyItem>(this, mBaiduMap);

        // 设置地图状态变化监听器（当地图缩放/移动时触发聚合计算）
        mBaiduMap.setOnMapStatusChangeListener(mClusterManager);

        // 设置地图标记点击监听器（点击单个标记或聚合点时触发）
        mBaiduMap.setOnMarkerClickListener(mClusterManager);

        //初始化JSON数据
        JSONArray equList = new JSONArray();
        JSONObject device1 = new JSONObject();
        JSONObject device2 = new JSONObject();
        try {
            device1.put("longitude", "120.123456");
            device1.put("latitude", "30.654321");
            device1.put("customerName", "设备1");
            device1.put("userAddr", "地址1");
            device1.put("customerPhone", "119");

            device2.put("longitude", "121.987654");
            device2.put("latitude", "31.234567");
            device2.put("customerName", "设备2");
            device2.put("userAddr", "地址2");
            device2.put("customerPhone", "120");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        equList.put(device1);
        equList.put(device2);


        List<MyItem> items = new ArrayList<MyItem>();
        //通过使用json_to_MyItem静态方法，将json数据转换成地图上点的格式MyItem
        try {
            items = MapDataProcessor.json_to_MyItem(equList);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        //把点添加到聚合类里
        mClusterManager.addItems(items);


        //点击聚合点触发
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {
                MapDataProcessor.handleClusterClick(cluster, mBaiduMap, mMapView);
                return false;
            }
        });


        //点击单个Marker点触发
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(MyItem item) {
                MapDataProcessor.handleClusterItemClick(item, getLayoutInflater(), mBaiduMap, MainActivity.this);
                return false;
            }
        });


    }

    private void initMap() throws Exception {
        // 获取地图控件引用
        mMapView = findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);

        // 定位初始化
        mLocationClient = new LocationClient(this);

        // 通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy); // 设置定位模式
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);

        // 设置locationClientOption
        mLocationClient.setLocOption(option);

        // 注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);

        // 开启地图定位图层
        mLocationClient.start();
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
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
    }

    private boolean checkPermissions() {
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    initMap();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                Toast.makeText(this, "定位权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null || mBaiduMap == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            mBaiduMap.setMyLocationData(locData);
        }
    }
}
