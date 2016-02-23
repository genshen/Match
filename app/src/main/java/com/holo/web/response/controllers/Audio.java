package com.holo.web.response.controllers;

import com.holo.web.request.RequestHeader;
import com.holo.web.response.core.Controller;
import com.holo.web.response.core.session.HttpSession;
import com.holo.web.tools.AndroidAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;

/**
 * Created by 根深 on 2016/2/21.
 */
public class Audio extends Controller {
    final String LOGIN = "login";

    public Audio(OutputStream os, RequestHeader header, HttpSession session) {
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
            data.put("musics", AndroidAPI.getMusicList());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        render("audio/index.html", data);
    }

}
