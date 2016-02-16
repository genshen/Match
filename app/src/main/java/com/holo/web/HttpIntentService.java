package com.holo.web;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.holo.web.response.core.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class HttpIntentService extends IntentService {
    ServerSocket serve;
    Socket clientSocket;

    public HttpIntentService() {
        super("HttpIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            serve = new ServerSocket(Config.HttpPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v("tag", "Listening for connection on port 8080 ....");
        try {
            while (!serve.isClosed()) {
                // when run [ serve.close(); ] the Exception will happen.
                clientSocket = serve.accept();
                (new ServerThread(clientSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Log.v("tag", "end");
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
