package com.holo.web.response.controllers;

import android.provider.MediaStore;

import com.holo.web.request.RequestHeader;
import com.holo.web.response.core.MediaController;
import com.holo.web.response.core.session.HttpSession;
import com.holo.web.tools.AndroidAPI;
import com.holo.web.tools.data_set.MediaInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;

/**
 * Created by 根深 on 2016/2/23.
 */
public class Video extends MediaController {
    final String FILE_ID = "id";

    public Video(OutputStream os, RequestHeader header, HttpSession session) {
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
            data.put("title", "视频");
            data.put("videos", AndroidAPI.getVideoList());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        render("video/index.html", data);
    }

    public void thumbMedia() {
        int login = session.getSessionInt(LOGIN);
        if (login != 1) {
            forbidden();
            return;
        }
        long id = getParams().getLong(FILE_ID);
        outThumbByOrigId(id, MediaStore.Video.Thumbnails.MINI_KIND, false);
    }

    public void playMedia() {
        int login = session.getSessionInt(LOGIN);
        if (login != 1) {
            forbidden();
            return;
        }

        long id = getParams().getLong(FILE_ID);
        MediaInfo mediainfo = AndroidAPI.getMediaLocation(id, 2);
        if (mediainfo.ilLegal()) { // file not exist
            notFound();
            return;
        }
        pullOut(mediainfo.file);
    }
}
