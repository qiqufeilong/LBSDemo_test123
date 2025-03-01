package com.example.lbsdemo_test;

import java.io.Serializable;

public class Equipment implements Serializable {
    private static final long serialVersionUID = 1L;

    // 地点的基本属性
    private double latitude; // 纬度
    private double longitude; // 经度
    private String customerName; // 客户名称
    private String customerPhone;
    private String userAddr; // 用户地址

    // 构造函数
    public Equipment() {}

    public Equipment( double latitude, double longitude, String customerName, String customerPhone, String userAddr) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.userAddr = userAddr;
    }

    // Getter 和 Setter 方法


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getUserAddr() {
        return userAddr;
    }

    public void setUserAddr(String userAddr) {
        this.userAddr = userAddr;
    }

    @Override
    public String toString() {
        return "Equipment{" +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", customerName='" + customerName + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", userAddr='" + userAddr + '\'' +
                '}';
    }
}