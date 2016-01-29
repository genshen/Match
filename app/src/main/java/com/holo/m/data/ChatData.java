package com.holo.m.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.holo.m.tools.files.FileManager;

/**
 * Created by 根深 on 2015/12/20.
 */
public class ChatData extends SQLiteOpenHelper {

    final String TableName = "Course_info";
    public static final String DATABASE_NAME = FileManager.getSDPath() + "/ChatData.db";
    String SQL_CREATE_COURSE = "create table chat_data(_id integer PRIMARY KEY AUTOINCREMENT," +
            "mac varchar," +        //mac地址
            "time integer," +       //时间
            "type integer," +       //文字?图?文件?
            "sender integer," +     //发送者或接收者
            "content varchar," +    //内容
            "state integer)";       //状态
    //ip

    public ChatData(Context context, int version) {
        super(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_COURSE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean isTableEmpty() {
        //  SELECT COUNT(*) FROM table_name
        String sql = "SELECT * FROM " + TableName;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        boolean empty = (cursor.getCount() == 0);
        cursor.close();
        return empty;
    }

}
