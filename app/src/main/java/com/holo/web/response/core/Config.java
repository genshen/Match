package com.holo.web.response.core;

import com.holo.m.files.FileManager;

/**
 * Created by cgs on 2016/2/11.
 */
public class Config {
    public static int HttpPort = 8080;
    public final static String TempPath = FileManager.getSDPath()+"/";
    public final static long ReleaseTime = 1455549428680L;

    public class Router {
        final static String defaultController = "Index";
        final static String defaultAction = "index";
    }

    public class ControllerConfig {
        final static String ControllerPackage = "com.holo.web.response.controllers.";
        final static String Action = "Action"; //can't change!
    }

    public class View {
        final static String VIEW = "core/views/";
        final static String VIEW_LAYOUT = "core/views/layout/main.html";
        final static short VIEW_LAYOUT_TOP = 3;
        final static short VIEW_LAYOUT_HEADER = 39;
        final static short VIEW_LAYOUT_BREAK = 7;
    }


}
