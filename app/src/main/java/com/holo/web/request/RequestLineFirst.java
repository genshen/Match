package com.holo.web.request;

import com.holo.web.tools.StringTools;

/**
 * Created by cgs on 2015/12/31.
 */
public class RequestLineFirst {
    String method, requestUri;
    boolean isHttp = false;
    RequestType requestType;

    /**
     * the first line just like {@code GET /bootstrap/css/bootstrap.min.css HTTP/1.1}
     * @param s the first line string
     *         set method requestType(html,json,css,js,medal...),url
     */
    public RequestLineFirst(String s) {
        if (s == null) return;
        String Re[] = s.split(" ");
        if (Re.length == 3) {
            isHttp = true;
            method = Re[0];
            String getUrl[] = Re[1].split("[?]", 2);
            requestUri = StringTools.NormalizUrl(getUrl[0]);
            requestType = StringTools.getRequestType(requestUri);
        }
    }

    /**
     *
     * @return normally GET or POST.
     */
    public String getMethod() {
        return this.method;
    }

    public String getRequestUri() {
        return this.requestUri;
    }

    public boolean isHttp() {
        return isHttp;
    }

    public RequestType getRequestType() {
        return requestType;
    }
}
