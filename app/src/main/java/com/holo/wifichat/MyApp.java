package com.holo.wifichat;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.holo.m.udp.UdpReceive;

/**
 * Created by 根深 on 2015/12/13.
 */

public class MyApp extends Application {
    public static String mac;
    public static UdpReceive ur;

    @Override
    public void onCreate() {
        super.onCreate();
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        mac = info.getMacAddress();


    }

    public boolean CheckNetwork() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connMgr.getActiveNetworkInfo();
        return info != null && info.isAvailable();
    }

}