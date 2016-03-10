package com.holo.web.response.controllers;

import com.holo.web.request.RequestHeader;
import com.holo.web.response.core.Controller;
import com.holo.web.response.core.session.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;

/**
 * Created by 根深 on 2016/3/9.
 */
public class Apps extends Controller {
    public Apps(OutputStream os, RequestHeader header, HttpSession session) {
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
            data.put("title", "应用");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        render("apps/index.html", data);
    }
}
