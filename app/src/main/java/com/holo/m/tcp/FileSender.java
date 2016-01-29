package com.holo.m.tcp;

import android.util.Log;

import com.holo.m.tools.files.BasicFileInformation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.List;

/**
 * Created by 根深 on 2016/1/25.
 */
public class FileSender extends Thread {
    final int BUFFSIZE = 2048;
    final int port = 12345;
    String ip;
    List<BasicFileInformation> files;

    BufferedInputStream is;
    BufferedOutputStream os;
    Socket sender;

    public FileSender(String ip, Serializable files) {
        this.ip = ip;
        this.files = (List<BasicFileInformation>) files;
    }

    /**
     * if (!file.exists()) {return;} 这里可能会有问题，对方可能会一直等待
     */
    @Override
    public void run() {
        Log.v("file", ">>>>>>>>>>>>>>>send start");
        try {
            Thread.sleep(100);
            int length = files.size();
            for (int i = 0; i < length; i++) {
                BasicFileInformation bfi = files.get(i);
                File file = new File(bfi.path);
                if (!file.exists()) continue;
                sendFile(file, bfi.getHeaderBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.v("file", ">>>>>>>>>>>>>>>send end");
    }

    public void sendFile(File file, byte[] header) {
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            sender = new Socket(ip, port);
            os = new BufferedOutputStream(sender.getOutputStream());
            byte[] buff = new byte[BUFFSIZE];
            int len;
            os.write(header);
            while ((len = is.read(buff)) != -1) {
                os.write(buff, 0, len);
            }
            is.close();
            os.flush();
            os.close();
            sender.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
