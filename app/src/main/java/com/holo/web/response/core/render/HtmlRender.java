package com.holo.web.response.core.render;

import com.holo.web.response.core.Config;
import com.holo.web.tools.AndroidAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by cgs on 2016/2/14.
 */
public class HtmlRender {
    final static byte[] BASE = ("<base href='http://" + Config.HOST + ":" + Config.HttpPort + "/'/>").getBytes();
    BufferedOutputStream bos;
    LayoutRender layoutRender;
    String template;
    JSONObject data;
    final static byte[] newline = {'\r', '\n'};

    public HtmlRender(String template, JSONObject data, LayoutRender.Layout layout, BufferedOutputStream bos) {
        this.bos = bos;
        this.template = template;
        this.layoutRender = new LayoutRender(layout,bos);
        this.data = data;
    }

    public void render() {
        layoutRender.continueRender();
        sendHtmlHead();
        layoutRender.continueRender();
        sendTemplate(template);
        layoutRender.continueRender();
        sendData(data);
        layoutRender.finishRender();
        try {
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(template);
    }

    private void sendHtmlHead() {
        try {
            bos.write(BASE);
            bos.write(newline);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendData(JSONObject data) {
        JSONObject json = new JSONObject();
        try {
            json.put("el", "html");
            json.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            bos.write(json.toString().getBytes());
            bos.write(newline);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendTemplate(String template) {
        try {
            InputStreamReader is_r = new InputStreamReader(AndroidAPI.getResource(Config.View.VIEW + template));
            BufferedReader br_r = new BufferedReader(is_r);
            while ((template = br_r.readLine()) != null) {
                bos.write(template.getBytes());
                bos.write(newline);
            }
//            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

