package com.holo.web.request;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cgs on 2015/12/31.
 */
public class RequestHeader {
    RequestLineFirst requestLineFirst;
    Map<String, String> Header = new HashMap<>();

    public RequestHeader(Socket clientSocket) {
        try {
            InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            requestLineFirst = new RequestLineFirst(reader.readLine());
            if (requestLineFirst.isHttp()) {
                setHeader(reader);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * separate every request line as key-value,and save it in a map
     *
     * @param reader
     */
    public void setHeader(BufferedReader reader) {
        String line;
        try {
            while (!(line = reader.readLine()).isEmpty()) {
                String str[] = line.split(":", 2);
                if (str.length == 2) {
                    Header.put(str[0], str[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHeaderValueByKey(String key) {
        return Header.get(key);
    }

    public RequestLineFirst getRequestLineFirst() {
        return requestLineFirst;
    }
}
