package com.holo.m.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.holo.m.files.FileManager;

/**
 * Created by 根深 on 2015/12/20.
 */
public class MatchData extends SQLiteOpenHelper {

    final String TableName = "Course_info";
    public static final String DATABASE_NAME = FileManager.getSDPath() + "/MatchData.db";
    String SQL_CREATE_CHAT_RECORD = "create table chat_data(_id integer PRIMARY KEY AUTOINCREMENT," +
            "mac varchar," +        //mac地址
            "time integer," +       //时间
            "type integer," +       //文字?图?文件?
            "sender integer," +     //发送者或接收者
            "content varchar," +    //内容
            "state integer)";       //状态
    //ip

    String SQL_CREATE_FILE_RECORD = "create table file_transfer_data (_id integer PRIMARY KEY AUTOINCREMENT," +
            "mac varchar," +        //mac地址
            "time integer," +       //时间
            "name varchar," +       //发送或接收者名
            "sender integer," +     //是否为发送者
            "transfer_time integer," +     //发送消耗时间
            "file_type integer," +     //文件类型：安装包，文档，图，音频，视频
            "file_size integer," +     //文件大小
            "file_name varchar," +     //文件名
            "file_path varchar," +     //文件路径
            "file_state integer," +      //状态 等待传输 正在传输 传输完成 已经删除 传输失败
            "remark varchar)";    //备注

    public MatchData(Context context, int version) {
        super(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CHAT_RECORD);
        db.execSQL(SQL_CREATE_FILE_RECORD);
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
