package com.holo.web.tools;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.holo.m.files.FileInfo;
import com.holo.m.files.FileManager;
import com.holo.m.tools.TimeTools;
import com.holo.m.tools.Tools;
import com.holo.web.response.core.render.HtmlRender;
import com.holo.web.tools.data_set.MediaInfo;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cgs on 2016/2/15.
 */
public class AndroidAPI {
    public static Context context;
    public static ContentResolver contentResolver;
    public static String entryCode;
    public static String SD_ROOT_DIR = FileManager.getSDPath();

    public static String getLocalIp() {
        return Tools.getLocalHostIp();
    }

    public static void initContext(Context con) {
        context = con;
        contentResolver = context.getContentResolver();
    }

    public static String newCode() {
        String u = "";
        for (int i = 0; i < 5; i++) {
            u += (int) (Math.random() * 10);
        }
        entryCode = u;
        return entryCode;
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

    public static long[] getSDcardInfo() {
        File f = Environment.getDataDirectory();
//        StatFs s = new StatFs(f.getPath());
        return new long[]{f.getFreeSpace(), f.getTotalSpace()};
    }

    /**
     * get video count stored in SD card
     *
     * @return count and total size
     */
    public static long[] getVideoCount() {
        return query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "duration > 30000", null);
    }

    /**
     * get Audio stored in SD card
     *
     * @return count and total size
     */
    public static long[] getAudioCount() {
        return query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "duration > 30000", null);
    }

    /**
     * get images stored in SD card
     *
     * @return count and total size
     */
    public static long[] getImagesCount() {
        return query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null);
    }

    /**
     * get document(.txt .doc .docx .xls .xlsx .pdf .ppt .pptx) stored in SD card
     *
     * @return count and total size
     */
    public static long[] getDocumentCount() {
        return query(MediaStore.Files.getContentUri("external"), DOC_SELECTION, null);
    }

    public static long[] getZipCount() {
        return query(MediaStore.Files.getContentUri("external"), ZIP_SELECTION, null);
    }


    static final String ID = "id";
    static final String THUMB_ID = "thumb_id";
    static final String TITLE = "title";
    static final String DATA = "data";
    static final String DISPLAY_NAME = "display_name";
    static final String ARTIST = "artist";
    static final String DURATION = "duration";
    static final String SIZE = "size";
    static final String SIZE_SHOW = "size_show";

    static final String DOC_SELECTION = "(" + MediaStore.Files.FileColumns.MIME_TYPE + "== 'text/plain') OR"
            + "(" + MediaStore.Files.FileColumns.MIME_TYPE + "== 'application/pdf') OR"
            + "(" + MediaStore.Files.FileColumns.MIME_TYPE + "== 'application/msword') OR"
            + "(" + MediaStore.Files.FileColumns.MIME_TYPE + "== 'pplication/vnd.ms-excel') OR"
            + "(" + MediaStore.Files.FileColumns.MIME_TYPE + "== 'application/vnd.ms-powerpoint')";
    static final String ZIP_SELECTION = MediaStore.Files.FileColumns.MIME_TYPE + "= 'application/zip'";
    static final String[] columns = new String[]{"COUNT(*)", "SUM(_size)"};

    private static long[] query(Uri uri, String selection, String order) {
        Cursor cursor = contentResolver.query(uri, columns, selection, null, order);
        if (cursor == null) {
            return new long[]{0, 0};
        }
        cursor.moveToFirst();
        long[] r = new long[]{cursor.getLong(0), cursor.getLong(1)};
        cursor.close();
        return r;
    }

    /**
     * get all images info
     *
     * @return JSONArray
     */
    public static JSONArray getImageList() {
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                null, null, null);
        List<Map<String, Object>> music_list = new ArrayList<>();
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                HashMap<String, Object> list_item = new HashMap<>();
                list_item.put(ID, cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
                list_item.put(DISPLAY_NAME, cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)));
//                list_item.put(MediaStore.Images.Media.DATA, cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
                list_item.put(SIZE_SHOW, FileInfo.getFileSize(size));
                music_list.add(list_item);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return new JSONArray(music_list);
    }

    /**
     * get all musics, including name,duration size,singer
     *
     * @return JSON data
     */
    public static JSONArray getMusicList() {
        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                "duration > 30000", null, MediaStore.Audio.Media.DISPLAY_NAME);
        List<Map<String, Object>> music_list = new ArrayList<>();
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                HashMap<String, Object> list_item = new HashMap<>();
                list_item.put(ID, cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                list_item.put(TITLE, cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                list_item.put(DISPLAY_NAME, cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
                list_item.put(ARTIST, cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                list_item.put(DURATION, TimeTools.getDurationDisplay(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))));
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                list_item.put(SIZE_SHOW, FileInfo.getFileSize(size));
                music_list.add(list_item);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return new JSONArray(music_list);
    }


    public static JSONArray getVideoList() {
        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
                "duration > 30000", null, MediaStore.Video.Media.DISPLAY_NAME);
        List<Map<String, Object>> video_list = new ArrayList<>();
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                HashMap<String, Object> list_item = new HashMap<>();
                list_item.put(ID, cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID)));
                list_item.put(TITLE, cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE)));
                list_item.put(DISPLAY_NAME, cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)));
                list_item.put(ARTIST, cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ARTIST)));
                list_item.put(DURATION, TimeTools.getDurationDisplay(cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))));
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                list_item.put(SIZE_SHOW, FileInfo.getFileSize(size));
                video_list.add(list_item);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return new JSONArray(video_list);
    }

    /**
     * @param origId  origId in images table
     * @param kind    MINI_KIND or MICRO_KIND
     * @param isImage image or video
     * @return Bitmap
     */
    public static Bitmap getThumb(long origId, int kind, boolean isImage) {
        if (isImage) {
            return MediaStore.Images.Thumbnails.getThumbnail(contentResolver, origId, kind, null);
        }
        return MediaStore.Video.Thumbnails.getThumbnail(contentResolver, origId, kind, null);
    }

    static Uri[] tables = {MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Files.getContentUri("external")};

    /**
     * get Medial file information by id and type
     *
     * @param id   id
     * @param type type
     * @return path and length and mime
     */
    public static MediaInfo getMediaLocation(long id, int type) {
        MediaInfo mediainfo = new MediaInfo();
        Cursor cursor = contentResolver.query(tables[type], new String[]{MediaStore.MediaColumns.DATA,
                        MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns.MIME_TYPE},
                MediaStore.MediaColumns._ID + "=" + id, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            mediainfo.file = new File(cursor.getString(0));
            mediainfo.mime = cursor.getString(2);
            return mediainfo;
        }
        if (cursor != null) {
            cursor.close();
        }
        return mediainfo;
    }

    static final String NAME = "name";
    static final String FILE_SIZE = "size";
    static final String LAST_MODIFY = "last_modify";
    static final String IS_DIR = "is_dir";
    //    final String FILE_ICON = "icon";

    public static JSONArray getFileList(String path) {
        return new JSONArray(getFileChildren(new File(SD_ROOT_DIR + path)));
    }

    private static List<Map<String, Object>> getFileChildren(File dir) {
        List<Map<String, Object>> fileList = new ArrayList<>();
        File[] children = dir.listFiles();
        for (File child : children) {
            if (child.isHidden()) continue;
            Map<String, Object> map = new HashMap<>();
            map.put(NAME, child.getName());
            boolean is_dir = child.isDirectory();
            map.put(IS_DIR, is_dir);
            if (!is_dir) {
                map.put(FILE_SIZE, child.length());
                map.put(LAST_MODIFY, child.lastModified());
            }
            fileList.add(map);
        }
        Collections.sort(fileList, new Comparator<Map<String, Object>>() {
                    @Override
                    public int compare(Map<String, Object> l, Map<String, Object> r) {
                        boolean l_dir = (boolean) l.get(IS_DIR);
                        boolean r_dir = (boolean) r.get(IS_DIR);
                        String l_name = (String) l.get(NAME);
                        String r_name = (String) r.get(NAME);
                        if (l_dir == r_dir) {
                            return String.CASE_INSENSITIVE_ORDER.compare(l_name, r_name);
                        } else if (l_dir) {
                            return 1;  // file should show first！
                        }
                        return -1;
                    }
                }
        );
        return fileList;
    }
}
