package com.holo.m.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by cgs on 2015/12/20.
 */
public class ChatDataManager {
    SQLiteDatabase course_db;
    public ChatData course;

    public ChatDataManager(Context context) {
        course = new ChatData(context, 1);
        course_db = course.getReadableDatabase();
    }

    public void insertContent(String mac, long time, int type, int sender, String content, int state) {
        content = content.replaceAll("\'","\'\'");
        String sql = "insert into  chat_data  (mac,time, type,sender,content,state) VALUES "
                + " (\'" + mac + "\',\'" + time + "\',\'" + type + "\',\'"
                + sender + "\',\'" + content + "\',\'" + state + "\')";
        course_db.execSQL(sql);
    }

    public List<HashMap<String, Object>> queryChatListByMac(String mac) {
        List<HashMap<String, Object>> mlist = new ArrayList<>();
        String sql = "select * from chat_data " + " where mac = \'" + mac + "\'";
        Cursor cursor = course_db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            HashMap<String, Object> listitem = new HashMap<>();
            listitem.put("time", cursor.getLong(cursor.getColumnIndex("time")));
            listitem.put("type", cursor.getShort(cursor.getColumnIndex("type")));
            listitem.put("sender", cursor.getShort(cursor.getColumnIndex("sender")) == 1);
            listitem.put("content", cursor.getString(cursor.getColumnIndex("content")));
            listitem.put("state", cursor.getShort(cursor.getColumnIndex("state")));
            mlist.add(listitem);
            cursor.moveToNext();
        }
        cursor.close();
        return mlist;
    }

    public void close() {
        course.close();
    }
}
