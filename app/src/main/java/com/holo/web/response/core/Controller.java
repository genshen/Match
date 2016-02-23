package com.holo.web.response.core;

import com.holo.web.request.RequestHeader;
import com.holo.web.request.data.GetData;
import com.holo.web.request.data.PostData;
import com.holo.web.response.core.session.HttpSession;
import com.holo.web.tools.URL;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by cgs on 2016/2/12.
 */
public class Controller {
    public boolean pjax;
    public BufferedOutputStream bos;
    public ResponseHeader responseHead;
    public RequestHeader requestHeader;
    protected HttpSession session;
    private GetData data_get = null;

    public Controller(OutputStream os, RequestHeader header, HttpSession session) {
        bos = new BufferedOutputStream(os);
        this.responseHead = new ResponseHeader();
        this.session = session;
        this.requestHeader = header;
        responseHead.setCookie(session);
        String pjax = header.getHeaderValueByKey("X-PJAX");
        this.pjax = pjax != null && pjax.equals("true");
    }

    public GetData getParams() {
        if (data_get == null) {
            data_get = new GetData(requestHeader.getRequestLineFirst().requestTail);
        }
        return data_get;
    }

    public boolean isPost() {
        return requestHeader.getRequestLineFirst().getMethod() == RequestHeader.RequestLineFirst.POST;
    }

    public PostData getPostData() {
        return requestHeader.getPostData();
    }

    public void redirect(String controller, String action) {
        responseHead.setState(ResponseHeader.Redirect);
        responseHead.setHeadValue("Location", URL.url(controller, action));
        responseHead.setHeadValue("Content-Length", "0");
        responseHead.Out(bos);
    }

    public void forbidden(){
        responseHead.setState(ResponseHeader.FORBIDDEN);
        responseHead.Out(bos);
    }

    public void render(String i) {
        responseHead.Out(bos);
        try {
            bos.write((i).getBytes());
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void renderLayout() {
        responseHead.Out(bos);
        HtmlRender html = new HtmlRender(bos);
        html.renderLayout();
    }

    public void render(String template, JSONObject data) {
        responseHead.Out(bos);
        HtmlRender html = new HtmlRender(template, data, bos);
        html.render();
    }

    public void renderJSON(String json) {
        responseHead.setHeadValue("Content-Type", "application/json; charset=utf-8");
        responseHead.setHeadValue("Content-Length", "" + json.length());
        responseHead.Out(bos);
        try {
            bos.write((json).getBytes());
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void outFile(String path) {
        byte[] b = new byte[1024];
        responseHead.Out(bos);
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
            while ((bis.read(b)) != -1) {
                bos.write(b);
            }
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}