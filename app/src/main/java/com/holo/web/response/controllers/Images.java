package com.holo.web.response.controllers;

import android.provider.MediaStore;

import com.holo.web.request.RequestHeader;
import com.holo.web.response.core.Controller;
import com.holo.web.response.core.session.HttpSession;
import com.holo.web.tools.AndroidAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;

/**
 * Created by 根深 on 2016/3/6.
 */
public class Images extends Controller {
    final String ID = "id";

    public Images(OutputStream os, RequestHeader header, HttpSession session) {
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
            data.put("title", "图片");
            data.put("images", AndroidAPI.getImageList());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        render("images/index.html", data);
    }

    public void  thumbMedia(){
        int login = session.getSessionInt(LOGIN);
        if (login != 1) {
            forbidden();
            return;
        }
        long id = getParams().getLong(ID);
        outThumbByOrigId(id, MediaStore.Images.Thumbnails.MINI_KIND,true);
    }
}
