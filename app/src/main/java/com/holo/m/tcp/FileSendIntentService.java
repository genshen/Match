package com.holo.m.tcp;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.holo.m.files.BasicFileInformation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.List;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class FileSendIntentService extends IntentService {
    public interface FileSendListener {
        void OnSendStart(int id);

        void OnSendFinish(int id, long transfer_time);

        void OnSendError(int id);
    }

    final int BUFFSIZE = 2048;
    final int port = 12345;
    public static FileSendListener fileSendListener = null;

    public FileSendIntentService() {
        super("FileSendIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * if (!file.exists()) {return;} 这里可能会有问题，对方可能会一直等待
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String ip = intent.getStringExtra("ip");
            List<BasicFileInformation> files = (List<BasicFileInformation>) intent.getSerializableExtra("files");
            Log.v("file", ">>>>>>>>>>>>>>>send start");
            try {
                Thread.sleep(100);
                int length = files.size();
                for (int i = 0; i < length; i++) {
                    BasicFileInformation bfi = files.get(i);
                    Notify(FILE_SEND_START, bfi.sender_id, 0);
                    File file = new File(bfi.path);
                    if (!file.exists()) {
                        Notify(FILE_SEND_ERROR,bfi.sender_id,0);
                        continue;
                    }
                    sendFile(ip, file, bfi.getHeaderBytes());
                    Notify(FILE_SEND_FINISH, bfi.sender_id, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.v("file", ">>>>>>>>>>>>>>>send end");
        }
    }

    final int FILE_SEND_START = 1, FILE_SEND_FINISH = 2, FILE_SEND_ERROR = 0;

    private void Notify(int tag, int id, int transfer_time) {
        // change database record
        if (fileSendListener != null) {
            switch (tag) {
                case FILE_SEND_START:
                    fileSendListener.OnSendStart(id);
                    break;
                case FILE_SEND_FINISH:
                    fileSendListener.OnSendFinish(id, transfer_time);
                    break;
                case FILE_SEND_ERROR:
                    fileSendListener.OnSendError(id);
                    break;
            }
        }
    }

    public void sendFile(String ip, File file, byte[] header) {
        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
            Socket sender = new Socket(ip, port);
            BufferedOutputStream os = new BufferedOutputStream(sender.getOutputStream());
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static void setOnFileSendListener(FileSendListener callback) {
        fileSendListener = callback;
    }
}
