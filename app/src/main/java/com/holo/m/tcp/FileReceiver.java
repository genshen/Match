package com.holo.m.tcp;

import android.util.Log;

import com.holo.m.tools.Tools;
import com.holo.m.files.FileManager;

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
    public interface ReceiveListener {
        void OnReceiveStart(int id);

        void OnReceiveFinish(int id, long time);

        void OnReceiveError(int id);
    }

    public static ReceiveListener receiveListener = null;
    final int BUFF_SIZE = 2048;
    byte[] file_buff = new byte[BUFF_SIZE];
    final int port = 12345, many;
    String ip;

    byte[] first = new byte[4];
    long size = 0;
    int receive_id = 0;
    String file_name;

    public FileReceiver(String ip, int many) {
        this.ip = ip;
        this.many = many;
    }

    @Override
    public void run() {
        File file;
        BufferedOutputStream os;
        BufferedInputStream is;
        Log.v("file", ">>>>>>>>>>>>>>>receive start");
        try {
            ServerSocket ss = new ServerSocket(port);
            for (int i = 0; i < many; i++) {
                Socket s = ss.accept();
                is = new BufferedInputStream(s.getInputStream());
                int len;
                len = is.read(first, 0, 4);   //check the first 4 bytes
                if (len != -1 && first[0] == 'F' && first[1] == 'I') {
                    byte[] header = new byte[(first[2] << 8) + first[3]];
                    len = is.read(header);  // notice, len may be -1
                    getHeader(header);
                    Notify(FILE_RECEIVE_START, receive_id, 0);
                    file = FileManager.CreateFile(file_name);
                    os = new BufferedOutputStream(new FileOutputStream(file));

                    while ((len = is.read(file_buff)) != -1) {
                        os.write(file_buff, 0, len);
                    }
                    os.flush();
                    os.close();
                }
                is.close();
                Notify(FILE_RECEIVE_FINISH, receive_id, 0);
            }
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
            Notify(FILE_RECEIVE_ERROR, receive_id, 0);
        }
        Log.v("file", ">>>>>>>>>>>>>>>receive end");
    }

    private void getHeader(byte[] b) {
        size = Tools.toLong(b);
        receive_id = Tools.toInt(b, 8);
//        System.out.println(l);

        byte[] des = new byte[b.length - 12];
        System.arraycopy(b, 12, des, 0, b.length - 12);

        file_name = new String(des);
//        System.out.println(s);
    }

    final int FILE_RECEIVE_START = 1, FILE_RECEIVE_FINISH = 2, FILE_RECEIVE_ERROR = 0;

    private void Notify(int tag, int id, int transfer_time) {
        // change database record
        if (receiveListener != null) {
            switch (tag) {
                case FILE_RECEIVE_START:
                    receiveListener.OnReceiveStart(id);
                    break;
                case FILE_RECEIVE_FINISH:
                    receiveListener.OnReceiveFinish(id, transfer_time);
                    break;
                case FILE_RECEIVE_ERROR:
                    receiveListener.OnReceiveError(id);
                    break;
            }
        }
    }

    public static void setReceiveListener(ReceiveListener callback) {
        receiveListener = callback;
    }

}
