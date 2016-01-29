package com.holo.m.message;

import java.io.Serializable;

/**
 * Created by 根深 on 2015/12/13.
 */
public class Messages implements Serializable {

    private final long time;
    private Object content;
    public String mac;
    public final String name;
    public final MessageType type;
    public String ip;

    public Messages(String mac, long time, String name, MessageType type, Object content) {
        this.time = time;
        this.name = name;
        this.type = type;
        this.mac = mac;
        this.content = content;
    }

    public String toString() {
        return time + name + type + ip +"\tcon::"+ content;
    }

    public Object getContent() {
        return this.content;
    }
    public long getTime() {
        return this.time;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
