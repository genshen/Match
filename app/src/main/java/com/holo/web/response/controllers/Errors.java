package com.holo.web.response.controllers;

import com.holo.web.request.RequestHeader;
import com.holo.web.response.core.Controller;
import com.holo.web.response.core.ResponseHeader;
import com.holo.web.response.core.session.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;

/**
 * Created by cgs on 2016/1/1.
 */
public class Errors extends Controller {

    public Errors(OutputStream os, RequestHeader header, HttpSession session) {
        super(os, header, session);
        responseHead.setState(ResponseHeader.NOT_FOUND);
    }

    public void notFoundAction(String url,boolean isHtml) {
        if(!isHtml){
            responseHead.Out(bos);// output 404 state only
            return;
        }
        JSONObject data = new JSONObject();
        try {
            data.put("url", url);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        render("index/error.html", data);
    }
}
