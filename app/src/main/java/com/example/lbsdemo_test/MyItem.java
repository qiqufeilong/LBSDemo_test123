package com.example.lbsdemo_test;// An highlighted block

import android.os.Bundle;

import com.baidu.mapapi.clusterutil.clustering.ClusterItem;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.model.LatLng;

/**
 * 每个Marker点，包含Marker点坐标、图标以及额外信息
 */
public class MyItem implements ClusterItem {
    private final LatLng mPosition;//点
    private Bundle buns;//额外信息
    public MyItem(LatLng latLng) {
        mPosition = latLng;
    }
    public MyItem(LatLng latLng,Bundle bun) {
        mPosition = latLng;
        buns=bun;
    }
    @Override
    public LatLng getPosition() {
        return mPosition;
    }
    @Override
    public Bundle getExtraInfo() {
        return buns;
    }
    @Override
    public BitmapDescriptor getBitmapDescriptor() {//点图标
        return BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
    }
}

