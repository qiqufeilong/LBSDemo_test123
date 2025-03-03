package com.example.lbsdemo_test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.clusterutil.clustering.Cluster;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapDataProcessor {
    public static List<MyItem> json_to_MyItem(JSONArray equList) throws JSONException {
        List<MyItem> items = new ArrayList<MyItem>();
        for(int i=0;i<equList.length();i++){
            JSONObject job = null;
            try {
                job = equList.getJSONObject(i);
            } catch (JSONException e) {
                Log.e("JSON_PARSE", "无效数据格式，跳过第" + i + "项", e);
                continue; // 跳过当前错误项继续执行2
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
        return items;
    }

    public static void handleClusterClick(Cluster<MyItem> cluster, BaiduMap baiduMap, MapView mapView) {
        List<MyItem> items = (List<MyItem>) cluster.getItems();
        LatLngBounds.Builder builder2 = new LatLngBounds.Builder();

        for (MyItem myItem : items) {
            builder2.include(myItem.getPosition());
        }

        LatLngBounds latlngBounds = builder2.build();
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLngBounds(
                latlngBounds,
                mapView.getWidth(),
                mapView.getHeight()
        );
        baiduMap.animateMapStatus(u);
    }

    public static void handleClusterItemClick(MyItem item, LayoutInflater layoutInflater, BaiduMap baiduMap, Context context) {
        if (item != null && item.getExtraInfo().get("json") != null) {
            String str = String.valueOf(item.getExtraInfo().get("json"));
            try {
                JSONObject job = new JSONObject(str);

                // 加载自定义布局
                View view = layoutInflater.inflate(R.layout.custom_info_bubble2, null);
                TextView textTitle = view.findViewById(R.id.marker_title);
                TextView textPhone = view.findViewById(R.id.marker_phone);
                TextView textAddress = view.findViewById(R.id.marker_address);

                // 设置标题
                SpannableString titleText = new SpannableString("名称：" + (job.has("customerName") ? job.getString("customerName") : "无"));
                textTitle.setText(titleText);

                // 设置电话
                SpannableString phone = new SpannableString("电话：" + (job.has("customerPhone") ? job.getString("customerPhone") : "无"));
                textPhone.setText(phone);

                // 设置地址
                SpannableString address = new SpannableString("地址：" + (job.has("userAddr") ? job.getString("userAddr") : "无"));
                textAddress.setText(address);

                // 创建设备对象
                Equipment equipment = new Equipment(
                        job.getDouble("latitude"),
                        job.getDouble("longitude"),
                        job.has("customerName") ? job.getString("customerName") : "无客户",
                        job.has("customerPhone") ? job.getString("customerPhone") : "无电话",
                        job.has("userAddr") ? job.getString("userAddr") : "无地址"
                );

                // 设置点击事件，跳转到详情页面
                view.setOnClickListener(v -> {
                    Intent intent = new Intent(context, Details.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("equipment", equipment);
                    intent.putExtras(bundle);
                    intent.putExtra("pageType", "detail");
                    context.startActivity(intent);
                });

                // 定义用于显示 InfoWindow 的坐标点
                LatLng pt = new LatLng(job.getDouble("latitude"), job.getDouble("longitude"));

                // 创建并显示 InfoWindow
                InfoWindow mInfoWindow = new InfoWindow(view, pt, -47);
                baiduMap.showInfoWindow(mInfoWindow);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
