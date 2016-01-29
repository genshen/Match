package com.holo.web.response;


import com.holo.web.request.RequestHeader;
import com.holo.web.request.RequestLineFirst;
import com.holo.web.request.RequestType;
import com.holo.web.response.error.NotFoundError;
import com.holo.web.tools.StringTools;

import java.io.*;
import java.text.Normalizer;

/**
 * Created by cgs on 2015/12/31.
 */
public class ResponseHttp {
    String request_url;
    String BasePath = "F:\\HttpFiles";
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
        File file = new File(BasePath + request_url);
        if (!file.exists()) {
            new NotFoundError(os);
            return;
        }
        try {
            long lastModify = file.lastModified();
            if (CheckModify(os, lastModify)) {
                FileInputStream in_s = new FileInputStream(file);
                byte byt[] = new byte[2048];
                int length;
                os.write(("HTTP/1.1 200 OK\r\nLast-Modified: " + StringTools.formatModify(lastModify) + "\r\n\r\n").getBytes());
                while ((length = in_s.read(byt)) != -1) {
                    os.write(byt, 0, length);
                }
                in_s.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("send finished\t" + request_url);
    }


    private void BuiltTextResponse(OutputStream os) {
        if (requestType == RequestType.HTML) {
            RenderHtml(os);
            return;
        }

        File file = new File(BasePath + request_url);
        if (!file.exists()) {
            new NotFoundError(os);
            return;
        }
        try {
            long lastModify = file.lastModified();
            if (CheckModify(os, lastModify)) {
                InputStreamReader is_r = new InputStreamReader(new FileInputStream(file), "UTF-8");
                BufferedReader br_r = new BufferedReader(is_r);
                String line;
                os.write(("HTTP/1.1 200 OK\r\nLast-Modified: " + StringTools.formatModify(lastModify) + "\r\n\r\n").getBytes());
                while ((line = br_r.readLine()) != null) {
                    os.write(line.getBytes("UTF-8"));
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
            os.write(("HTTP/1.1 200 OK\r\n\r\n" + "<html><head><meta charset='utf-8'/></head>"
                    +"<h1>Hello world<br>@梁泰琳@马成宏</h1></html>").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean CheckModify(OutputStream os, long lastModify) {
        if (!StringTools.CheckModify(header.getHeaderValueByKey("If-Modified-Since"), lastModify)) {
            try {
                os.write(("HTTP/1.1 304 OK\r\n\r\n").getBytes());
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
