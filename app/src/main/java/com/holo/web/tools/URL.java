package com.holo.web.tools;

import com.holo.web.response.core.Config;

/**
 * Created by 根深 on 2016/2/19.
 */
public class URL {
    static String Base = "http://"+Config.HOST + (Config.HttpPort == 80 ? "" : ":" + Config.HttpPort);

    public static String url(String controller, String action) {
        return Base + "/" + controller + "/" + action + ".html";
    }
}
