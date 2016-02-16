package com.holo.m.tcp;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.holo.m.voice.Voice;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageReceiverService extends IntentService {

    ServerSocket serve;
    Socket socket;
    BufferedOutputStream os;
    BufferedInputStream is;
    File file;
    final int BUFF_SIZE = 2048;
    byte[] receive_buff = new byte[BUFF_SIZE];
    int len = 0;
    final String base_path = Voice.BASE_PATH;
    byte[] first = new byte[4];

    public MessageReceiverService() {
        super("MessageReceiverService");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        try {
            serve = new ServerSocket(8086);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v("tag", "new voice or image file");
        try {
            while (!serve.isClosed()) {
                // when run [ serve.close(); ] the Exception will happen.
                socket = serve.accept();
                is = new BufferedInputStream(socket.getInputStream());

                len = is.read(first, 0, 4);   //check the first 4 bytes
                if (len != -1 && first[0] == 'V' && first[1] == 'I') {
                    byte[] header = new byte[(first[2] << 8) + first[3]];
                    len = is.read(header);
                    String file_name =  new String(header);
                    os = new BufferedOutputStream(new FileOutputStream(base_path+file_name));
                    while ((len = is.read(receive_buff)) != -1) {
                        os.write(receive_buff, 0, len);
                    }
                    os.flush();
                    os.close();
                }
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            serve.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Log.v("tag", "onDestroy");
    }
}
