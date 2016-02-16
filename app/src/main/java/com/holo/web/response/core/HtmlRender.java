package com.holo.web.response.core;

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
    final static String host = AndroidAPI.getLocalIp();
    final static byte[] BASE = ("<base href='http://" + host + ":" + Config.HttpPort + "/'/>").getBytes();
    BufferedOutputStream bos;
    String template;
    JSONObject data;
    byte[] newline = {'\r', '\n'};

    public HtmlRender(String template, JSONObject data, BufferedOutputStream bos) {
        this.bos = bos;
        this.template = template;
        this.data = data;
    }

    public void render() {
        try {
            InputStreamReader is_r = new InputStreamReader(AndroidAPI.getResource(Config.View.VIEW_LAYOUT));
            BufferedReader br_r = new BufferedReader(is_r);
            String line;
            sendLayout(Config.View.VIEW_LAYOUT_TOP, br_r);
            sendHtmlHead();
            sendLayout(Config.View.VIEW_LAYOUT_HEADER, br_r);
            sendTemplates(template);
            sendLayout(Config.View.VIEW_LAYOUT_BREAK, br_r);
            sendData(data);
            //send out left content
            while ((line = br_r.readLine()) != null) {
                bos.write(line.getBytes());
                bos.write(newline);
            }
            br_r.close();
            is_r.close();
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

    private void sendLayout(short lines, BufferedReader br_r) {
        String line;
        for (short k = lines; k > 0; k--) {
            try {
                line = br_r.readLine();
                bos.write(line.getBytes());
                bos.write(newline);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    private void sendTemplates(String template) {
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

