package com.holo.m.tools;

import android.os.Build;
import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by cgs on 2015/12/13.
 */
public class Tools {

    public static String getName() {
        return getMachineInfo() + ("");
    }

    public static String getMachineInfo() {
        return Build.MODEL;
    }

    public static String getLocalHostIp() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (inet.hasMoreElements()) {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ip.getHostAddress())) {
                        return ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            Log.e("error", "fail to get local ip address");
            e.printStackTrace();
        }
        return "";
    }

    public static String getBroadCastIP() {
//        return getLocalHostIp().substring(0,getLocalHostIp().lastIndexOf(".") + 1)+ "255";
        return "255.255.255.255";
    }

    public static byte[] toByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

    public static Object toObject(byte[] bytes) {
        Object obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return obj;
    }

    /**
     * transform byte array to long
     *
     * @param b array
     * @return long
     */
    public static long toLong(byte[] b) {
        long l = b[0];
        l <<= 8;
        l += b[1]&0xFF;
        l <<= 8;
        l += b[2]&0xFF;
        l <<= 8;
        l += b[3]&0xFF;
        l <<= 8;
        l += b[4]&0xFF;
        l <<= 8;
        l += b[5]&0xFF;
        l <<= 8;
        l += b[6]&0xFF;
        l <<= 8;
        l += b[7]&0xFF;
        return l;
    }

    /**
     * transform long to byte array
     * @param l long
     * @return byte[]
     */
    public static byte[] toByteArray(long l) {
        byte[] b = new byte[8];
        b[7] = (byte) (l % 256);
        l >>>= 8;
        b[6] = (byte) (l % 256);
        l >>>= 8;
        b[5] = (byte) (l % 256);
        l >>>= 8;
        b[4] = (byte) (l % 256);
        l >>>= 8;
        b[3] = (byte) (l % 256);
        l >>>= 8;
        b[2] = (byte) (l % 256);
        l >>>= 8;
        b[1] = (byte) (l % 256);
        l >>>= 8;
        b[0] = (byte)l;
        return b;
    }
}
