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
public class MatchDataManager {
    SQLiteDatabase match_db;
    final String FILE_TABLE_NAME = "file_transfer_data";
    public MatchData database;
    private static int file_table_auto_increase_top = 0;

    public MatchDataManager(Context context) {
        database = new MatchData(context, 1);
        match_db = database.getReadableDatabase();
        file_table_auto_increase_top = getTableTop(FILE_TABLE_NAME);
    }

    /**
     * @param mac     mac address
     * @param time    time(second)
     * @param type    type: text,image,file,voice
     * @param sender  1 for sender,0 for receiver
     * @param content text message or file name or file path;
     * @param length    voice duration or total file size
     * @param state   sent? received? read?
     */
    public void insertChatRecord(String mac, long time, int type, int sender, String content, long length, int state) {
        content = content.replaceAll("\'", "\'\'");
        String sql = "insert into  chat_data  (mac,time,type,sender,content,length,state) VALUES "
                + " (\'" + mac + "\',\'" + time + "\',\'" + type + "\',\'"
                + sender + "\',\'" + content + "\',\'" + length + "\',\'" + state + "\')";
        match_db.execSQL(sql);
    }

    /**
     * inset a record to table file_transfer_data
     *
     * @param mac           mac
     * @param time          the time message sent
     * @param name          sender name
     * @param sender        am i a sender? 1 fro sender,0 foe receiver
     * @param transfer_time the time spent to transfer the file
     * @param file_type     file type such as doc, image ,music
     * @param file_size     file_size (bits)
     * @param file_name     file_name ,file_name may not the acture name of file,it is the name shown on UI
     * @param file_path     file stored path
     * @param file_state    file_state,such as sent? received?transfer error?...
     * @param remark        remark
     * @return _id which record insert to
     */
    public int insertFileTransferRecord(String mac, long time, String name, int sender, long transfer_time,
                                        int file_type, long file_size, String file_name, String file_path,
                                        int file_state, String remark) {
        String sql = "insert into file_transfer_data (_id,mac,time, name,sender,transfer_time,file_type,file_size,"
                + "file_name,file_path,file_state,remark) VALUES"
                + " (\'" + (++file_table_auto_increase_top) + "\',\'" + mac + "\',\'" + time + "\',\'" + name + "\',\'"
                + sender + "\',\'" + transfer_time + "\',\'" + file_type + "\',\'" + file_size + "\',\'"
                + file_name + "\',\'" + file_path + "\',\'" + file_state + "\',\'" + remark + "\')";
        match_db.execSQL(sql);
        return file_table_auto_increase_top;
    }

    public List<HashMap<String, Object>> queryAllFileTransferRecord() {
        List<HashMap<String, Object>> mList = new ArrayList<>();
        String sql = "select * from file_transfer_data " + " where 1";
        Cursor cursor = match_db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            HashMap<String, Object> listitem = new HashMap<>();
            listitem.put("id", cursor.getInt(cursor.getColumnIndex("_id")));                //int
            listitem.put("time", cursor.getLong(cursor.getColumnIndex("time")));            //long
            listitem.put("name", cursor.getString(cursor.getColumnIndex("name")));          // string
            listitem.put("sender", cursor.getInt(cursor.getColumnIndex("sender")) == 1);    //boolean
            listitem.put("transfer_time", cursor.getLong(cursor.getColumnIndex("transfer_time")));
            listitem.put("file_type", cursor.getShort(cursor.getColumnIndex("file_type")));
            listitem.put("file_size", cursor.getLong(cursor.getColumnIndex("file_size")));
            listitem.put("file_name", cursor.getString(cursor.getColumnIndex("file_name")));
            listitem.put("file_path", cursor.getString(cursor.getColumnIndex("file_path")));
            listitem.put("file_state", cursor.getShort(cursor.getColumnIndex("file_state")));
            mList.add(listitem);
            cursor.moveToNext();
        }
        cursor.close();
        return mList;
    }


    public List<HashMap<String, Object>> queryChatListByMac(String mac) {
        List<HashMap<String, Object>> mlist = new ArrayList<>();
        String sql = "select * from chat_data " + " where mac = \'" + mac + "\'";
        Cursor cursor = match_db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            HashMap<String, Object> listitem = new HashMap<>();
            listitem.put("time", cursor.getLong(cursor.getColumnIndex("time")));
            listitem.put("type", cursor.getShort(cursor.getColumnIndex("type")));
            listitem.put("sender", cursor.getShort(cursor.getColumnIndex("sender")) == 1);
            listitem.put("content", cursor.getString(cursor.getColumnIndex("content")));
            listitem.put("length", cursor.getLong(cursor.getColumnIndex("length")));
            listitem.put("state", cursor.getShort(cursor.getColumnIndex("state")));
            mlist.add(listitem);
            cursor.moveToNext();
        }
        cursor.close();
        return mlist;
    }

    private int getTableTop(String table) {
        String sql = "select max(_id) from " + table;
        Cursor cursor = match_db.rawQuery(sql, null);
        cursor.moveToFirst();
        int max = cursor.getInt(cursor.getColumnIndex("max(_id)"));
        cursor.close();
        return max;
    }

    public void close() {
        database.close();
    }
}
