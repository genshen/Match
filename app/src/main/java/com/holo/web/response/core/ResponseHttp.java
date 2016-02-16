package com.holo.web.response.core;

import com.holo.web.request.RequestHeader;
import com.holo.web.request.RequestType;
import com.holo.web.response.error.NotFoundError;
import com.holo.web.tools.AndroidAPI;
import com.holo.web.tools.StringTools;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Created by cgs on 2015/12/31.
 */
public class ResponseHttp {
    String request_url;
    RequestHeader header;
    final RequestType requestType;

    public ResponseHttp(RequestHeader rh) {
        header = rh;
        request_url = rh.getRequestLineFirst().getRequestUri();
        requestType = rh.getRequestLineFirst().getRequestType();
    }

    public void startResponse(OutputStream outputStream) {
        if (requestType != RequestType.MEDIA) {
            BuiltTextResponse(outputStream);
        } else {
            BuiltMediaResponse(outputStream);
        }
    }

    private void BuiltMediaResponse(OutputStream os) {
//        File file = new File(Config.BasePath + request_url);
        InputStream is = AndroidAPI.getResource(request_url);
        if (is == null) {
            new NotFoundError(os);
            return;
        }
        try {
            long lastModify = Config.ReleaseTime;
            if (CheckModify(os, lastModify)) {
                BufferedInputStream bis = new BufferedInputStream(is);
                byte byt[] = new byte[2048];
                int length;
                os.write(("HTTP/1.1 200 OK\r\nLast-Modified: " + StringTools.formatModify(lastModify) + "\r\n\r\n").getBytes());
                while ((length = bis.read(byt)) != -1) {
                    os.write(byt, 0, length);
                }
                bis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("send finished\t" + request_url);
    }

    final byte[] newline = {'\r', '\n'};

    private void BuiltTextResponse(OutputStream os) {
        if (requestType == RequestType.HTML) {
            RenderHtml(os);
            return;
        }

        InputStream is = AndroidAPI.getResource(request_url);
        if (is == null) {
            new NotFoundError(os);
            return;
        }
        try {
            long lastModify = Config.ReleaseTime;
            if (CheckModify(os, lastModify)) {
                InputStreamReader is_r = new InputStreamReader(is, "UTF-8");
                BufferedReader br_r = new BufferedReader(is_r);
                String line;
                os.write(("HTTP/1.1 200 OK\r\nLast-Modified: " + StringTools.formatModify(lastModify) + "\r\n\r\n").getBytes());
                while ((line = br_r.readLine()) != null) {
                    os.write(line.getBytes("UTF-8"));
                    os.write(newline);
                }
                br_r.close();
                is_r.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("send finished\t" + request_url);
    }

    private void RenderHtml(OutputStream os) {
        Router router = new Router(request_url);
        try {
            Class c = Class.forName(Config.ControllerConfig.ControllerPackage + router.controller);
            Constructor constructor = c.getDeclaredConstructor(OutputStream.class);
            Object obj = constructor.newInstance(os);
            Method method = c.getDeclaredMethod(router.action + Config.ControllerConfig.Action);
            method.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
            NotFoundError notFound = new NotFoundError(os);
            notFound.render(request_url);
        }
    }

    private boolean CheckModify(OutputStream os, long lastModify) {
        if (!StringTools.CheckModify(header.getHeaderValueByKey("If-Modified-Since"), lastModify)) {
            try {
                os.write(("HTTP/1.1 304 Not Modified\r\n\r\n").getBytes());
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
