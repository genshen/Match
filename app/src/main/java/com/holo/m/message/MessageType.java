package com.holo.m.message;

/**
 * Created by 根深 on 2015/12/13.
 */
public enum MessageType {
    ONLINE, ONLINE_REPLY, FileSendRequest, FileSendReply, NORMAL_MESSAGE;
    //NORMAL_MESSAGE ->text message
    final static int D = 2;
    public static boolean isChatMessage(int index){
        return index >= D;
    }
}