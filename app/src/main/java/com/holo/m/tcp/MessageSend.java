package com.holo.m.tcp;

import com.holo.m.voice.Voice;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.net.Socket;

/**
 * Created by 根深 on 2016/2/7.
 */
public class MessageSend extends Thread {

    String path, file_name, ip;
    final int BUFF_SIZE = 2048;
    byte[] send_buff = new byte[BUFF_SIZE];

    public MessageSend(String ip, String file_name) {
        this.path = Voice.BASE_PATH + file_name;
        this.ip = ip;
        this.file_name = file_name;
    }

    @Override
    public void run() {
        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(path));
            Socket sender = new Socket(ip, 8086);
            BufferedOutputStream os = new BufferedOutputStream(sender.getOutputStream());
            int len;
            os.write(writeHeader(os));
            while ((len = is.read(send_buff)) != -1) {
                os.write(send_buff, 0, len);
            }
            is.close();
            os.flush();
            os.close();
            sender.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    private byte[] writeHeader(BufferedOutputStream os) {
        short len = (short) file_name.length();
        byte[] h = new byte[len + 4];
        h[0] = 'V';
        h[1] = 'I';
        h[2] = (byte) (len / 256);
        h[3] = (byte) (len % 256);
        byte[] name = file_name.getBytes();

        System.arraycopy(name, 0, h, 4, len);// src, src_pos, des, des_pos, length
        return h;
    }
}
