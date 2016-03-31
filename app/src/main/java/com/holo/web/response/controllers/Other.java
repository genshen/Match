package com.holo.web.response.controllers;

import com.holo.web.request.RequestHeader;
import com.holo.web.response.core.Controller;
import com.holo.web.response.core.session.HttpSession;
import com.holo.web.tools.AndroidAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;

/**
 * Created by 根深 on 2016/2/23.
 */
public class Other extends Controller {

    public Other(OutputStream os, RequestHeader header, HttpSession session) {
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
            data.put("title", "文档与压缩包");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        render("other/index.html", data);
    }

    public void listAction() {
        int login = session.getSessionInt(LOGIN);
        if (login != 1) {
            forbidden();
            return;
        }

        JSONObject data = new JSONObject();
        try {
            data.put("document", AndroidAPI.getDocumentList());
            data.put("zip", AndroidAPI.getZipList());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        renderJSON(data);
    }
}
