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

        // 检查并请求定位权限
        if (checkPermissions()) {
            try {
                initMap();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            requestPermissions();
        }
        // 定义点聚合管理类ClusterManager
        mClusterManager = new ClusterManager<MyItem>(this, mBaiduMap);
        mBaiduMap.setOnMapStatusChangeListener(mClusterManager);
        // 设置maker点击时的响应
        mBaiduMap.setOnMarkerClickListener(mClusterManager);

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
        for(int i=0;i<equList.length();i++){
            JSONObject job = null;
            try {
                job = equList.getJSONObject(i);
            } catch (JSONException e) {
                Log.e("JSON_PARSE", "无效数据格式，跳过第" + i + "项", e);
                continue; // 跳过当前错误项继续执行1
            }
            //获取经纬度添加地图标注
            String lo = null;
            String la = null;
            try {
                la = job.has("latitude")?job.getString("latitude"):"";
                lo = job.has("longitude")?job.getString("longitude"):"";
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            if(!TextUtils.isEmpty(lo)&&!TextUtils.isEmpty(la)){
                LatLng point =  new LatLng(Double.valueOf(la), Double.valueOf(lo));
                //添加额外信息
                Bundle bundle = new Bundle();
                bundle.putString("json",job.toString());
                MyItem myItem = new MyItem(point,bundle);
                items.add(myItem);

            }
        }
        mClusterManager.addItems(items);//把点添加到聚合类里

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {//点击聚合点触发
            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {
                //没用上Toast.makeText(getActivity(), "有" + cluster.getSize() + "个点", Toast.LENGTH_SHORT).show();
                List<MyItem> items = (List<MyItem>) cluster.getItems();
                LatLngBounds.Builder builder2 = new LatLngBounds.Builder();
                int i=0;
                for(MyItem myItem : items){
                    builder2 = builder2.include(myItem.getPosition());
                }
                LatLngBounds latlngBounds = builder2.build();
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngBounds(latlngBounds,mMapView.getWidth(),mMapView.getHeight());
                mBaiduMap.animateMapStatus(u);
                return false;
            }
        });
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {//点击单个Marker点触发
            @Override
            public boolean onClusterItemClick(MyItem item) {
                if(item!=null&&item.getExtraInfo().get("json")!=null){
                    String str = String.valueOf(item.getExtraInfo().get("json"));
                    //点击弹出气泡窗口，显示一些信息。点击窗口可以跳转到详情页面
                    try {
                        JSONObject job = new JSONObject(str);
                        View view = getLayoutInflater().inflate(R.layout.custom_info_bubble2, null);
                        TextView text_title = (TextView) view.findViewById(R.id.marker_title);
                        TextView text_phone = (TextView) view.findViewById(R.id.marker_phone);
                        TextView text_address = (TextView) view.findViewById(R.id.marker_address);
                        SpannableString titleText = new SpannableString("名称："+(job.has("customerName")?job.getString("customerName"):"无"));
                        /*titleText.setSpan(new ForegroundColorSpan(R.color.generalColor), 0, titleText.length(), 0);*/
                        text_title.setText(titleText);
                        SpannableString phone = new SpannableString("电话："+(job.has("customerPhone")?job.getString("customerPhone"):"无"));
                        text_phone.setText(phone);
                        SpannableString address = new SpannableString("地址："+(job.has("userAddr")?job.getString("userAddr"):"无"));
                        text_address.setText(address);
                        //点击进入详情页面
                        Equipment equipment = new Equipment(
                                job.getDouble("latitude"),
                                job.getDouble("longitude"),
                                job.has("customerName") ? job.getString("customerName") : "无客户",
                                job.has("customerPhone") ? job.getString("customerPhone") : "无电话",
                                job.has("userAddr") ? job.getString("userAddr") : "无地址"
                        );
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //使用json数据传递参数到详情页面。
                                Intent intent;
                                intent = new Intent(getApplicationContext(), Details.class);

                                Bundle bundle = new Bundle();
                                // Serializable传递对象
                                bundle.putSerializable("equipment", equipment);
                                intent.putExtras(bundle);
                                intent.putExtra("pageType","detail");//用于标别查看页面
                                startActivity(intent);
                            }
                        });
                        //定义用于显示该InfoWindow的坐标点
                        LatLng pt = new LatLng(job.getDouble("latitude"),job.getDouble("longitude"));
                        //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
                        InfoWindow mInfoWindow = new InfoWindow(view, pt, -47);
                        //显示InfoWindow
                        mBaiduMap.showInfoWindow(mInfoWindow);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
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
        //定义Maker坐标点
        LatLng point = new LatLng(39.963175, 116.400244);
//构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon);
//构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option1 = new MarkerOptions()
                .position(point)
                .icon(bitmap);
//在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option1);
        //用来构造InfoWindow的Button
        Button button = new Button(getApplicationContext());
        button.setBackgroundResource(R.drawable.popup);
        button.setText("InfoWindow");

//构造InfoWindow
//point 描述的位置点
//-100 InfoWindow相对于point在y轴的偏移量
        InfoWindow mInfoWindow = new InfoWindow(button, point, -100);

//使InfoWindow生效
        mBaiduMap.showInfoWindow(mInfoWindow);
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
