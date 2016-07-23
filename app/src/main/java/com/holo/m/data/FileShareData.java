package com.holo.m.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.holo.m.files.BasicFileInformation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by 根深 on 2016/3/30.
 */
public class FileShareData {
    SQLiteDatabase match_db;
    public MatchData database;

    public FileShareData(Context context) {
        database = new MatchData(context, 1);
        match_db = database.getReadableDatabase();
    }

    public void close() {
        database.close();
    }

    public boolean isOpen() {
        return match_db.isOpen();
    }

    public Map<String, Object> getSharedResourceVerifyHash(String resourceHash, int id) {
        if (resourceHash == null || resourceHash.isEmpty()) {
            return null;
        }
        String sql = "select * from " + RESOURCE_SHARE.TABLE_NAME + " where _id = " + id + " limit 1"; // todo hash need
        Cursor cursor = match_db.rawQuery(sql, null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            Map<String, Object> m = new HashMap<>();
            m.put(RESOURCE_SHARE._ID, cursor.getInt(cursor.getColumnIndex(RESOURCE_SHARE._ID)));
            m.put(RESOURCE_SHARE.TITLE, cursor.getString(cursor.getColumnIndex(RESOURCE_SHARE.TITLE)));
            m.put(RESOURCE_SHARE.FILE_HASH, cursor.getString(cursor.getColumnIndex(RESOURCE_SHARE.FILE_HASH)));
            m.put(RESOURCE_SHARE.AUTHOR, cursor.getString(cursor.getColumnIndex(RESOURCE_SHARE.AUTHOR)));
            m.put(RESOURCE_SHARE.CREATE_TIME, formatDate(cursor.getLong(cursor.getColumnIndex(RESOURCE_SHARE.CREATE_TIME))));
            m.put(RESOURCE_SHARE.DESCRIBE, cursor.getString(cursor.getColumnIndex(RESOURCE_SHARE.DESCRIBE)));
            cursor.close();
            return m;
        }
        cursor.close();
        return null;
    }

    public Map<String, Object> getSharedResourceById(long id) {
        String sql = "select * from " + RESOURCE_SHARE.TABLE_NAME + " where _id = " + id + " limit 1";
        Cursor cursor = match_db.rawQuery(sql, null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            Map<String, Object> m = new HashMap<>();
            m.put(RESOURCE_SHARE._ID, cursor.getInt(cursor.getColumnIndex(RESOURCE_SHARE._ID)));
            m.put(RESOURCE_SHARE.TITLE, cursor.getString(cursor.getColumnIndex(RESOURCE_SHARE.TITLE)));
            m.put(RESOURCE_SHARE.AUTHOR, cursor.getString(cursor.getColumnIndex(RESOURCE_SHARE.AUTHOR)));
            m.put(RESOURCE_SHARE.FILE_COUNT, cursor.getString(cursor.getColumnIndex(RESOURCE_SHARE.FILE_COUNT)));
            m.put(RESOURCE_SHARE.ALL_SIZE, cursor.getString(cursor.getColumnIndex(RESOURCE_SHARE.ALL_SIZE)));
            m.put(RESOURCE_SHARE.CREATE_TIME, formatDate(cursor.getLong(cursor.getColumnIndex(RESOURCE_SHARE.CREATE_TIME))));
            m.put(RESOURCE_SHARE.DESCRIBE, cursor.getString(cursor.getColumnIndex(RESOURCE_SHARE.DESCRIBE)));
            cursor.close();
            return m;
        }
        cursor.close();
        return null;
    }

    private String formatDate(long hour) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-DD", Locale.ENGLISH);
        return format.format(new Date(hour * 3600));
    }

    public long createShareResource(String title, String author, String describe) {
        long time_now = System.currentTimeMillis() / 3600;
        ContentValues value = new ContentValues();
        value.put(RESOURCE_SHARE.TITLE, title);
        value.put(RESOURCE_SHARE.FILE_HASH, generateHash());
        value.put(RESOURCE_SHARE.AUTHOR, author);
        value.put(RESOURCE_SHARE.MODIFY_TIME, time_now);
        value.put(RESOURCE_SHARE.CREATE_TIME, time_now);
        value.put(RESOURCE_SHARE.FILE_COUNT, 0);
        value.put(RESOURCE_SHARE.ALL_SIZE, 0);
        value.put(RESOURCE_SHARE.DESCRIBE, describe);
        return match_db.insert(RESOURCE_SHARE.TABLE_NAME, null, value);
    }

    private String generateHash() {  //todo
        return "Aaeds2s32";
    }

    public void getSharedResourceList(ArrayList<Map<String, Object>> shareList) {
        String sql = "select * from " + RESOURCE_SHARE.TABLE_NAME + " where 1";
        Cursor cursor = match_db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Map<String, Object> item = new HashMap<>();
            item.put(RESOURCE_SHARE._ID, cursor.getLong(cursor.getColumnIndex(RESOURCE_SHARE._ID)));
            item.put(RESOURCE_SHARE.TITLE, cursor.getString(cursor.getColumnIndex(RESOURCE_SHARE.TITLE)));
            item.put(RESOURCE_SHARE.DESCRIBE, cursor.getString(cursor.getColumnIndex(RESOURCE_SHARE.DESCRIBE)));
            //    item.put(RESOURCE_SHARE.TITLE,cursor.getString(cursor.getColumnIndex(RESOURCE_SHARE.TITLE)));
            shareList.add(item);
            cursor.moveToNext();
        }
        cursor.close();
    }

    public void getSharedFileList(ArrayList<Map<String, Object>> shareFileList, long id) {
        String sql = "select * from " + SHARE_DETAIL.TABLE_NAME + " where " + SHARE_DETAIL.PARENT_ID + " = " + id;
        Cursor cursor = match_db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Map<String, Object> item = new HashMap<>();
            item.put(SHARE_DETAIL._ID, cursor.getLong(cursor.getColumnIndex(SHARE_DETAIL._ID)));
            item.put(SHARE_DETAIL.TITLE, cursor.getString(cursor.getColumnIndex(SHARE_DETAIL.TITLE)));
            item.put(SHARE_DETAIL.FILE_SIZE, cursor.getLong(cursor.getColumnIndex(SHARE_DETAIL.FILE_SIZE)));
            item.put(SHARE_DETAIL.TIME, cursor.getLong(cursor.getColumnIndex(SHARE_DETAIL.TIME)));
            shareFileList.add(item);
            cursor.moveToNext();
        }
        cursor.close();
    }

    public void addSharedFiles(long id, List<BasicFileInformation> fileList) {
        long time_now = System.currentTimeMillis() / 1000;
        ContentValues value = new ContentValues();
        value.put(SHARE_DETAIL.PARENT_ID, id);
        for (BasicFileInformation f : fileList) {
            value.put(SHARE_DETAIL.TITLE, f.name);
            value.put(SHARE_DETAIL.FILE_HASH, generateHash());
            value.put(SHARE_DETAIL.TIME, time_now);
            value.put(SHARE_DETAIL.FILE_SIZE, f.size);
            value.put(SHARE_DETAIL.DATA, f.path);
            value.put(SHARE_DETAIL.TYPE, 0);
            match_db.insert(SHARE_DETAIL.TABLE_NAME, null, value);
        }
    }

    public static class RESOURCE_SHARE {
        // download count
        public static final String TABLE_NAME = "file_share_record";
        public static final String _ID = "_id";
        public static final String TITLE = "title";
        public static final String FILE_HASH = "file_hash";
        public static final String AUTHOR = "author";
        public static final String MODIFY_TIME = "modify_time";
        public static final String CREATE_TIME = "create_time";
        public static final String FILE_COUNT = "file_count";
        public static final String ALL_SIZE = "all_size";
        public static final String DESCRIBE = "describe";
    }

    public static class SHARE_DETAIL {
        public static final String TABLE_NAME = "file_share_detail";
        public static final String _ID = "_id";
        public static final String TITLE = "title";
        public static final String FILE_HASH = "file_hash";
        public static final String PARENT_ID = "parent_id";
        public static final String TIME = "time";
        public static final String FILE_SIZE = "file_size";
        public static final String DATA = "data";
        public static final String TYPE = "type";
    }

}
