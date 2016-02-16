package com.holo.m.udp;

import com.holo.m.message.MessageType;
import com.holo.m.message.Messages;
import com.holo.m.tools.TimeTools;
import com.holo.m.tools.Tools;
import com.holo.match.MyApp;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by 根深 on 2015/12/13.
 */
public class UdpSend {
    public final String MultiCastAddress = "255.255.255.255";

    public void online() {
        Messages msg = new Messages(MyApp.mac, TimeTools.getTime(), Tools.getName(), MessageType.ONLINE, null);
        Send send = new Send(msg, false, MultiCastAddress);
        send.start();
    }

    public void onlineReply(String ip) {
        Messages msg = new Messages(MyApp.mac, TimeTools.getTime(), Tools.getName(), MessageType.ONLINE_REPLY, null);
        Send send = new Send(msg, false, ip);
        send.start();
    }

    public void sendMessage(String ip, long time, String message) {
        Messages msg = new Messages(MyApp.mac, time, Tools.getName(), MessageType.TEXT_MESSAGE, message);
        Send send = new Send(msg, false, ip);
        send.start();
    }

    public void sendVoiceRequest(String ip, long time, long length, String file_name) {
        Messages msg = new Messages(MyApp.mac, time, Tools.getName(), MessageType.VOICE_MESSAGE_Request, file_name);
        msg.setLength(length);
        Send send = new Send(msg, false, ip);
        send.start();
    }

    /**
     * set length = 0 as default,this value is useless
     */
    public void sendVoiceReply(String ip, String file_name) {
        Messages msg = new Messages(MyApp.mac, TimeTools.getTime(), Tools.getName(), MessageType.VOICE_MESSAGE_Reply, file_name);
        Send send = new Send(msg, false, ip);
        send.start();
    }

    public void sendFileRequest(String ip, Serializable files) {
        Messages msg = new Messages(MyApp.mac, TimeTools.getTime(), Tools.getName(), MessageType.FileSendRequest, files);
        Send send = new Send(msg, false, ip);
        send.start();
    }

    public void sendFileReply(String ip, Serializable filePath) {
        Messages msg = new Messages(MyApp.mac, TimeTools.getTime(), Tools.getName(), MessageType.FileSendReply, filePath);
        Send send = new Send(msg, false, ip);
        send.start();
    }

    class Send extends Thread {
        Messages msg;
        String des_ip;
        boolean multi_cast;

        Send(Messages msg, boolean multi_cast, String des_ip) {
            this.msg = msg;
            this.multi_cast = multi_cast;
            this.des_ip = des_ip;
        }

        public void run() {
            try {
                byte[] data = Tools.toByteArray(msg);

                if (multi_cast) {
                    MulticastSocket multicast = new MulticastSocket();
                    InetAddress address = InetAddress.getByName(des_ip); // 必须使用D类地址
                    multicast.joinGroup(address); // 以D类地址为标识，加入同一个组才能实现广播

//                  MulticastSocket multicast = new MulticastSocket();
                    multicast.setTimeToLive(32);
                    DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(des_ip), 2425);
                    multicast.send(packet);
                    multicast.close();
                } else {
//                    System.out.println("send>>>>>>>>>>>>>>>>>>>>>>>" + msg.toString());
                    DatagramSocket ds = new DatagramSocket();//2426
                    DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(des_ip), 2426);
                    packet.setData(data);
                    ds.send(packet);
                    ds.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}