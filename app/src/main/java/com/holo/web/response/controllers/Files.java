package com.holo.web.response.controllers;

import com.holo.web.request.RequestHeader;
import com.holo.web.response.core.Controller;
import com.holo.web.response.core.ResponseHeader;
import com.holo.web.response.core.session.HttpSession;
import com.holo.web.tools.AndroidAPI;

import java.io.OutputStream;

/**
 * Created by 根深 on 2016/2/20.
 */
public class Files extends Controller {
    final String LOGIN = "login";
    final String FILE_TYPE = "type";
    final String FILE_ID = "id";

    public Files(OutputStream os, RequestHeader header, HttpSession session) {
        super(os, header, session);
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
        int file_id = getParams().getInt(FILE_ID);
        Object f[] = AndroidAPI.getMediaLocation(file_id, file_type);
//      Content-Length
        responseHead.setHeadValue(ResponseHeader.Content_Transfer_Encoding, "binary");
        responseHead.setHeadValue(ResponseHeader.Content_Type, "application/octet-stream");
        outFile((String)f[0]);
    }
}
