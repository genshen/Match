package com.holo.web.response.core;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.holo.web.request.RequestHeader;
import com.holo.web.request.data.GetData;
import com.holo.web.request.data.PostData;
import com.holo.web.response.core.render.HtmlRender;
import com.holo.web.response.core.render.LayoutRender;
import com.holo.web.response.core.session.HttpSession;
import com.holo.web.tools.AndroidAPI;
import com.holo.web.tools.URL;
import com.holo.web.tools.data_set.MediaInfo;

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
    public final String LOGIN = "login";
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

    public void forbidden() {
        responseHead.setState(ResponseHeader.FORBIDDEN);
        responseHead.Out(bos);
    }

    public void notFound() {
        responseHead.setState(ResponseHeader.NOT_FOUND);
        responseHead.Out(bos);
    }

    public void badRequest() {
        responseHead.setState(ResponseHeader.Bad_Request);
        responseHead.Out(bos);
    }

    /**just rend a string to browser*/
    public void render(String i) {
        responseHead.Out(bos);
        try {
            bos.write((i).getBytes());
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void render(String template,LayoutRender.Layout layout, JSONObject data) {
        responseHead.Out(bos);
        HtmlRender html = new HtmlRender(template,data,layout,bos);
        html.render();
    }

    public void render(String template, JSONObject data) {
        render(template, LayoutRender.DEFAULT_LAYOUT, data);
    }

    public void renderJSON(String json) {
        byte[] jsonBytes = (json).getBytes();
        responseHead.setHeadValue("Content-Type", "application/json; charset=utf-8");
        responseHead.setHeadValue("Content-Length", "" + jsonBytes.length);
        responseHead.Out(bos);
        try {
            bos.write(jsonBytes);
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void renderJSON(JSONObject json){
        renderJSON(json.toString());
    }

    public void outFile(@NonNull String path,String mime){
       outFile(new MediaInfo(path,mime));
    }

    public void outFile(MediaInfo mediaInfo) {
        if(mediaInfo.ilLegal()){
            notFound();
            return;
        }
        byte[] b = new byte[1024];
        responseHead.setHeadValue(ResponseHeader.Content_Type, mediaInfo.mime);
        responseHead.setHeadValue(ResponseHeader.Content_Length, mediaInfo.file.length() + "");
        responseHead.Out(bos);
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(mediaInfo.file));
            while ((bis.read(b)) != -1) {
                bos.write(b);
            }
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void outThumbByOrigId(long origId, int kind,boolean isImage){
        Bitmap bitmap = AndroidAPI.getThumb(origId, kind,isImage);
        if(bitmap == null){
            notFound();
            return;
        }
        responseHead.setHeadValue(ResponseHeader.Content_Type,"image/png");
        bitmap.compress(Bitmap.CompressFormat.PNG,50,bos);
    }
}