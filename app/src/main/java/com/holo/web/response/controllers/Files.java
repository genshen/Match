package com.holo.web.response.controllers;

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
 * Created by 根深 on 2016/2/20.
 */
public class Files extends Controller {
    final String FILE_TYPE = "type";
    final String FILE_ID = "id";

    public Files(OutputStream os, RequestHeader header, HttpSession session) {
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
            data.put("title", "文件");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        render("files/index.html", data);
    }

    public void file_listAction() {
        int login = session.getSessionInt(LOGIN);
        if (login != 1) {
            forbidden();
            return;
        }

        JSONObject data = new JSONObject();
        try {
            data.put("files", AndroidAPI.getFileList());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        renderJSON(data);
    }

    public void uploadAction() {
        int login = session.getSessionInt(LOGIN);
        if (login != 1) {
            forbidden();
            return;
        }

        if (isPost()) {
            getPostData().getString("file");
            renderJSON("{\"status\":\"ok\"}");
        }
    }

    //not html file returned
    public void downloadMedia() {
        int login = session.getSessionInt(LOGIN);
        if (login != 1) {
            forbidden();
            return;
        }

        int file_type = getParams().getInt(FILE_TYPE);
        long file_id = getParams().getLong(FILE_ID);
        MediaInfo mediainfo = AndroidAPI.getMediaLocation(file_id, file_type);
        if(mediainfo.ilLegal()){ // file not exist
            notFound();
            return;
        }
        mediainfo.mime = "application/octet-stream";
        responseHead.setHeadValue(ResponseHeader.Content_Transfer_Encoding, "binary");
        outFile(mediainfo);
    }
}
