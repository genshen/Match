package com.holo.web;

import com.holo.web.request.RequestHeader;
import com.holo.web.response.ResponseHttp;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by ���� on 2016/1/1.
 */
public class ServerThread extends Thread {
    Socket clientSocket;

    public ServerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        run();
    }

    @Override
    public void run() {
        RequestHeader rh = new RequestHeader(clientSocket);
        try {
            if (rh.getRequestLineFirst().isHttp()) {
                ResponseHttp responseHttp = new ResponseHttp(rh);
                responseHttp.startResponse(clientSocket.getOutputStream());
                //  clientSocket.getOutputStream().write(responseHttp.BuiltResponse());
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
