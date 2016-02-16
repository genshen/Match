package com.holo.web.response.controllers;

import com.holo.web.response.core.Controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;

/**
 * Created by ���� on 2016/2/12.
 */
public class Index extends Controller {
    String string;

    public Index(OutputStream os) {
        super(os);
    }

    public void indexAction() {
        JSONObject data = new JSONObject();
        try {
            data.put("title", "hello");
            JSONArray array = new JSONArray("[{ text: 'Learn JavaScript' },{ text: 'Learn Vue.js' },"
                    + "{ text: 'Build Something Awesome' }]");
            data.put("todos", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        render("index/index.html", data);
    }
}
