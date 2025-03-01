package com.example.lbsdemo_test;
import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.common.BaiduMapSDKException;

public class Demo extends Application {
    private static final String TAG = "Demo";

    public static final String SP_NAME = "privacy";
    public static final String SP_KEY = "ifAgree";

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.setAgreePrivacy(this, true);
        SDKInitializer.initialize(getApplicationContext());
        LocationClient.setAgreePrivacy(true);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext

        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);

    }


}