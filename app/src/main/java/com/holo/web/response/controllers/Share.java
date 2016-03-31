package com.holo.web.response.controllers;

import com.holo.m.data.FileShareData;
import com.holo.web.request.RequestHeader;
import com.holo.web.response.core.MediaController;
import com.holo.web.response.core.render.LayoutRender;
import com.holo.web.response.core.session.HttpSession;
import com.holo.web.tools.AndroidAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.util.Map;

/**
 * Created by 根深 on 2016/3/30.
 */
public class Share extends MediaController {
    final String RESOURCE_HASH = "h";
    final String RESOURCE_ID = "id";

    public Share(OutputStream os, RequestHeader header, HttpSession session) {
        super(os, header, session);
    }

    public void indexAction() {
        String resource = getParams().getString(RESOURCE_HASH);
        int id = getParams().getInt(RESOURCE_ID);
        FileShareData fileShareData = new FileShareData(AndroidAPI.context);
        Map<String,Object> m = fileShareData.getSharedResourceVerifyHash(resource, id);
        fileShareData.close();
        if(m == null){
            notFound(); // todo render error page
            return;
        }
        JSONObject data = new JSONObject();
        try {
            data.put("title", "文件共享");
            data.put("shared", new JSONObject(m));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        render("share/index.html", LayoutRender.SHARE_LAYOUT, data);
    }

    public void shareAction() {
        JSONObject data = new JSONObject();
        try {
            data.put("title", "文件共享");
            data.put("shared", "n");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        render("share/share.html", LayoutRender.SHARE_LAYOUT, data);
    }
}
