package com.holo.web.response.core;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by cgs on 2016/2/12.
 */
public class Controller {
    public BufferedOutputStream bos;
    public ResponseHeader responseHead;

    public Controller(OutputStream os) {
        bos = new BufferedOutputStream(os);
        this.responseHead = new ResponseHeader();
    }

    private void renderHead() {
        try {
            bos.write((responseHead.first_line + "\r\n").getBytes());
            Iterator<Map.Entry<String, String>> en = responseHead.heads.entrySet().iterator();
            Map.Entry<String, String> m;
            while (en.hasNext()) {
                m = en.next();
                bos.write((m.getKey() + ":" + m.getValue() + "\r\n").getBytes());
            }
            bos.write("\r\n".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void render(String i) {
        renderHead();
        try {
            bos.write((i).getBytes());
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void render(String template, JSONObject data) {
        renderHead();
        HtmlRender html = new HtmlRender(template, data,bos);
        html.render();
    }

}