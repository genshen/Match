package com.holo.m.tcp;

import android.util.Log;

import com.holo.m.tools.Tools;
import com.holo.m.tools.files.FileManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 根深 on 2016/1/25.
 */
public class FileReceiver extends Thread {
    final int BUFFSIZE = 2048;
    final int port = 12345, many;
    String ip;

    byte[] first = new byte[4];
    long size = 0;
    String file_name;

    public FileReceiver(String ip, int many) {
        this.ip = ip;
        this.many = many;
    }

    @Override
    public void run() {
        File file;
        BufferedOutputStream os;
        Log.v("file", ">>>>>>>>>>>>>>>receive start");
        try {
            ServerSocket ss = new ServerSocket(port);
            for (int i = 0; i < many; i++) {
                Socket s = ss.accept();
                BufferedInputStream is = new BufferedInputStream(s.getInputStream());

                byte[] buff = new byte[BUFFSIZE];
                int len;
                len = is.read(first, 0, 4);   //check the first 4 bytes
                if (len != -1 && first[0] == 'F' && first[1] == 'I') {
                    len = (first[2] << 8) + first[3];
                    byte[] header = new byte[len];
                    len = is.read(header);  // notice, len may be -1
                    setHeader(header);
                    file = FileManager.CreateFile(file_name);
                    os = new BufferedOutputStream(new FileOutputStream(file));

                    while ((len = is.read(buff)) != -1) {
                        os.write(buff, 0, len);
                    }
                    os.flush();
                    os.close();
                }
                is.close();
            }
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.v("file", ">>>>>>>>>>>>>>>receive end");
    }

    public void setHeader(byte[] b) {
        size = Tools.toLong(b);
//        System.out.println(l);

        byte[] des = new byte[b.length - 8];
        System.arraycopy(b, 8, des, 0, b.length - 8);

        file_name = new String(des);
//        System.out.println(s);
    }

}
