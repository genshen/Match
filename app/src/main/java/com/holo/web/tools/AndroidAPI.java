package com.holo.web.tools;

import android.content.Context;

import com.holo.m.tools.Tools;
import com.holo.web.response.core.HtmlRender;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by cgs on 2016/2/15.
 */
public class AndroidAPI {
    public static Context context;

    public static String getLocalIp() {
        return Tools.getLocalHostIp();
    }

    public static void initContext(Context con) {
        context = con;
    }

    /**
     * used by {@link HtmlRender#sendTemplates(String template)}. {@link HtmlRender#render()}
     * {@link com.holo.web.response.core.ResponseHttp#BuiltMediaResponse(OutputStream os)}.
     * {@link com.holo.web.response.core.ResponseHttp#BuiltTextResponse(OutputStream os)}.
     *
     * @param filename filepath
     * @return
     */
    public static InputStream getResource(String filename) {
        if (filename.charAt(0) == '/' || filename.charAt(0) == '\\') {
            filename = filename.substring(1);
        }
        try {
            return context.getResources().getAssets().open(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
