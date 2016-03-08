package com.holo.web.response.controllers;

import com.holo.web.request.RequestHeader;
import com.holo.web.response.core.Controller;
import com.holo.web.response.core.session.HttpSession;
import com.holo.web.tools.AndroidAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;

/**
 * Created by cgs on 2016/2/12.
 */
public class Index extends Controller {

    public Index(OutputStream os, RequestHeader header, HttpSession session) {
        super(os, header, session);
    }

    public void indexAction() {
        int login = session.getSessionInt(LOGIN);
        if (login != 1) {
            redirect("index", "login");
            return;
        }

        JSONObject data = new JSONObject();
        try {
            data.put("title", "首页");
            data.put("apps", toJSON(new long[]{0, 0}));
            data.put("images", toJSON(AndroidAPI.getImagesCount()));
            data.put("audio", toJSON(AndroidAPI.getAudioCount()));
            data.put("video", toJSON(AndroidAPI.getVideoCount()));
            data.put("document", toJSON(AndroidAPI.getDocumentCount()));
            data.put("zip", toJSON(AndroidAPI.getZipCount()));
            long[] info = AndroidAPI.getSDcardInfo();
            data.put("free_space", info[0]);
            data.put("total_space", info[1]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        render("index/index.html", data);
    }

    public void loginAction() {
        //if has login
        int login = session.getSessionInt(LOGIN);
        if (login == 1) {
            redirect("index", "index");
            return;
        }

        if (isPost()) {
            if (getPostData().getString("code").equals(AndroidAPI.entryCode)) {
                session.setSession(LOGIN, 1);
                render("1");
            } else {
                render("0");
            }
        } else {
            JSONObject data = new JSONObject();
            try {
                data.put("title", "登录认证");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            render("index/login.html", data);
        }
    }

    private JSONObject toJSON(long[] info) {
        JSONObject data = new JSONObject();
        try {
            data.put("count", info[0]);
            data.put("size", info[1]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
}
