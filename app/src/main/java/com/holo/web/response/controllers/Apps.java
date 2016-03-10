package com.holo.web.response.controllers;

import android.graphics.Bitmap;

import com.holo.web.request.RequestHeader;
import com.holo.web.response.core.Controller;
import com.holo.web.response.core.ResponseHeader;
import com.holo.web.response.core.session.HttpSession;
import com.holo.web.tools.AndroidAPI;
import com.holo.web.tools.data_set.MediaInfo;

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
            data.put("apps", AndroidAPI.getAppList());
            data.put("apks", AndroidAPI.getApkList());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        render("apps/index.html", data);
    }

    public void downloadMedia() {
        int login = session.getSessionInt(LOGIN);
        if (login != 1) {
            forbidden();
            return;
        }

        int index = getParams().getInt("index");
        MediaInfo mediainfo = AndroidAPI.getAppMediaInfo(index);
        mediainfo.mime = "application/octet-stream";
        responseHead.setHeadValue(ResponseHeader.Content_Transfer_Encoding, "binary");
        outFile(mediainfo);
    }

    public void app_iconMedia() {
        int login = session.getSessionInt(LOGIN);
        if (login != 1) {
            forbidden();
            return;
        }
        int index = getParams().getInt("index");
        outIconBitmap(AndroidAPI.loadAppIcon(index));
    }

    public void apk_iconMedia() {
        int login = session.getSessionInt(LOGIN);
        if (login != 1) {
            forbidden();
            return;
        }
        long id = getParams().getLong("id");
        outIconBitmap(AndroidAPI.loadAppIcon(id));
    }

    private void outIconBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            notFound();
            return;
        }
        responseHead.setHeadValue(ResponseHeader.Content_Type, "image/png");
        responseHead.Out(bos);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
    }
}
