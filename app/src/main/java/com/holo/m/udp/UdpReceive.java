package com.holo.m.udp;

import com.holo.m.message.MessageType;
import com.holo.m.message.Messages;
import com.holo.m.tools.Tools;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


/**
 * Created by 根深 on 2015/12/13.
 */
public class UdpReceive {
//    public final String MultiCastAddress = "239.0.0.1";
//    MulticastSocket multicastSocket;

    public static String chat_ip = null;
    DatagramSocket ds;
    byte[] buff = new byte[1024 * 4];

    ReceiveMessageListener receiveMessagelistener;
    ReceiveChatMessageListener receiveChatMessagelistener;

    public interface ReceiveMessageListener {
        void onReceiveMessage(Messages msg);
    }

    public interface ReceiveChatMessageListener {
        void onReceiveThisChatMessage(Messages msg);
    }

    /**
     * init socket
     */
    public UdpReceive() {
        try {
//            multicastSocket = new MulticastSocket(2425);
            ds = new DatagramSocket(2426);
        } catch (IOException e) {
            e.printStackTrace();
            //set error;
//            handler.sendEmptyMessage(0x099);
        }
        /*是否接受多播?*/
//        MReceive mr = new MReceive();
//        mr.start();
    }

    public void start() {
        FReceive fr = new FReceive();
        fr.start();
    }

    class FReceive extends Thread {
        public void run() {
            try {
                while (!ds.isClosed()) {
//                    fType(receiveMsg());
                    receiveMsg();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    class MReceive extends Thread {
//        public void run() {
//            try {
//                while (true) {
//                    fType(receiveMsg());
//                    receiveMultiMsg();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    private boolean receiveMultiMsg() {
//        DatagramPacket dp = new DatagramPacket(buff, buff.length);
//        try {
//            InetAddress address = InetAddress.getByName(MultiCastAddress);
//            multicastSocket.joinGroup(address);
//
//            dp.setData(buff);
//            multicastSocket.receive(dp); // 接收数据，同样会进入阻塞状态
//            byte[] data = new byte[dp.getLength()]; // 从buffer中截取收到的数据
//            System.arraycopy(buff, 0, data, 0, data.length);
//
//            Messages msg = (Messages) Tools.toObject(data);
//            String ip = dp.getAddress() + "";
//            if (ip.charAt(0) == '/') {
//                ip = ip.substring(1);
//            }
//            msg.setIp(ip);
//
//            Message m = new Message();
//            m.what = 0x101;
//            m.obj = msg;
//            handler.sendMessage(m);
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    /**
     * when a UDP message comes, it will call onReceiveMessage or onReceiveThisChatMessage
     * to send a notification
     *
     * @return the message just received
     * @throws Exception
     */
    public Messages receiveMsg() throws Exception {
        DatagramPacket dp = new DatagramPacket(buff, buff.length);
        dp.setData(buff);
        ds.receive(dp);

        byte[] data2 = new byte[dp.getLength()];
        System.arraycopy(buff, 0, data2, 0, data2.length);
        Messages msg = (Messages) Tools.toObject(data2);
        String ip = dp.getAddress() + "";
        if (ip.charAt(0) == '/') {
            ip = ip.substring(1);
        }
        msg.setIp(ip);

        System.out.println(">>>>>>>>>>>>>>>>>>>>>" + msg.toString());
        if (ip.equals(chat_ip) && MessageType.isChatMessage(msg.type.ordinal())) { // chat view! is normal message?
            receiveChatMessagelistener.onReceiveThisChatMessage(msg);
            return msg;
        }
        receiveMessagelistener.onReceiveMessage(msg);
        return msg;
    }

    public void close() {
//        multicastSocket.close();
        ds.close();
    }

    public void setChatIp(String chat_ip) {
        this.chat_ip = chat_ip;
    }

    /**
     * set onReceiveMessageListener interface function
     */
    public void setOnReceiveMessageListener(ReceiveMessageListener callback) {
        receiveMessagelistener = callback;
    }

    /**
     * set onReceiveChatMessageListener interface function in chatActivity
     */
    public void setOnReceiveChatMessageListener(ReceiveChatMessageListener callback) {
        receiveChatMessagelistener = callback;
    }
}
