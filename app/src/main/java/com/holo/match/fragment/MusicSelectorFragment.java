package com.holo.match.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.holo.m.files.BasicFileInformation;
import com.holo.m.files.FileInfo;
import com.holo.match.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 根深 on 2016/2/1.
 */
public class MusicSelectorFragment extends Fragment implements AbsListView.MultiChoiceModeListener, AdapterView.OnItemClickListener {
    int video_count = 0;
    ListView listView;
    List<Map<String, Object>> music_video_list;
    MusicVideoAdapter musicVideoAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_music_selector, container, false);
        music_video_list = new ArrayList<>();
        getVideoList(music_video_list);
        video_count = music_video_list.size();
        getMusicList(music_video_list);

        musicVideoAdapter = new MusicVideoAdapter(getContext(), music_video_list);
        listView = (ListView) rootView.findViewById(R.id.music_selector_listview);
        listView.setAdapter(musicVideoAdapter);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setOnItemClickListener(this);
        listView.setMultiChoiceModeListener(this);
        return rootView;
    }

    final String TITLE = "title";
    final String DATA = "data";
    final String DISPLAY_NAME = "display_name";
    final String ARTIST = "artist";
    final String DURATION = "duration";
    final String SIZE = "size";
    final String SIZE_SHOW = "size_show";

    private List<Map<String, Object>> getMusicList(List<Map<String, Object>> music_video_list) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, "duration > 30000", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        // todo duration > 30000
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                HashMap<String, Object> list_item = new HashMap<>();
                list_item.put(TITLE, cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                list_item.put(DATA, cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                list_item.put(DISPLAY_NAME, cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
                list_item.put(ARTIST, cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                list_item.put(DURATION, getDurationShow(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))));
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                list_item.put(SIZE, size);
                list_item.put(SIZE_SHOW, FileInfo.getFileSize(size));
                music_video_list.add(list_item);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return music_video_list;
    }

    private List<Map<String, Object>> getVideoList(List<Map<String, Object>> music_video_list) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null, "duration > 30000", null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        // todo duration > 30000
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                HashMap<String, Object> list_item = new HashMap<>();
                list_item.put(TITLE, cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE)));
                list_item.put(DATA, cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)));
                list_item.put(DISPLAY_NAME, cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)));
                list_item.put(ARTIST, cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ARTIST)));
                list_item.put(DURATION, getDurationShow(cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))));
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                list_item.put(SIZE, size);
                list_item.put(SIZE_SHOW, FileInfo.getFileSize(size));
                music_video_list.add(list_item);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return music_video_list;
    }

    private String getDurationShow(int duration) {
        duration /= 1000;
        int second = duration % 60;
        duration /= 60;
        int minute = duration % 60;
        duration /= 60;
        int hour = duration % 60;
        return (hour == 0 ? "" : hour + ":") +
                (minute < 10 ? "0" + minute + ":" : minute + ":") +
                (second < 10 ? "0" + second : second);
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        mode.setTitle(String.format(getString(R.string.have_selected_count), listView.getCheckedItemCount()));
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.file_selector_app, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        mode.setTitle(String.format(getString(R.string.have_selected_count), listView.getCheckedItemCount()));
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_send:
                List<BasicFileInformation> musics = new ArrayList<>();
                for (int i = musicVideoAdapter.getCount() - 1; i >= 0; i--) {
                    if (listView.isItemChecked(i)) {
                        final Map<String, Object> m = music_video_list.get(i);
                        musics.add(new BasicFileInformation(m.get(DATA).toString(),
                                m.get(DISPLAY_NAME).toString(), (long) m.get(SIZE)));
                    }
                }
                startResult(musics);
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        for (int i = musicVideoAdapter.getCount() - 1; i >= 0; i--) {
            listView.setItemChecked(i, false);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Map<String, Object> m = music_video_list.get(position);
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_file_selector_send_ack)
                .setMessage(R.string.dialog_file_selector_send_ack_message)
                .setPositiveButton(R.string.dialog_file_selector_send_ack_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int witch) {
                        List<BasicFileInformation> music = new ArrayList<>();
                        music.add(new BasicFileInformation(m.get(DATA).toString(),
                                m.get(DISPLAY_NAME).toString(), (long) m.get(SIZE)));
                        startResult(music);
                    }
                })
                .setNegativeButton(R.string.dialog_file_selector_send_ack_cancel, null).show();
    }

    private void startResult(List<BasicFileInformation> musics) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("files", (Serializable) musics);
        intent.putExtras(bundle);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    private class MusicVideoAdapter extends BaseAdapter {
        List<Map<String, Object>> music_video_list;
        Context context;

        public MusicVideoAdapter(Context context, List<Map<String, Object>> images) {
            this.music_video_list = images;
            this.context = context;
        }

        @Override
        public int getCount() {
            return music_video_list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            Map<String, Object> item = music_video_list.get(position);
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = getLayoutInflater(null).inflate(R.layout.listview_music_video_selector, null);
                holder.icon = (AppCompatImageView) convertView.findViewById(R.id.music_video_icon);
                holder.title = (AppCompatTextView) convertView.findViewById(R.id.music_video_selector_title);
                holder.artist = (AppCompatTextView) convertView.findViewById(R.id.music_video_selector_artist);
                holder.duration = (AppCompatTextView) convertView.findViewById(R.id.music_video_selector_duration);
                holder.size = (AppCompatTextView) convertView.findViewById(R.id.music_video_selector_size);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.icon.setImageResource(position < video_count ? R.drawable.filesystem_icon_movie : R.drawable.filesystem_icon_music);
            holder.title.setText(item.get(TITLE).toString());
            holder.artist.setText(item.get(ARTIST).toString());
            holder.duration.setText(item.get(DURATION).toString());
            holder.size.setText(item.get(SIZE_SHOW).toString());
            return convertView;
        }

        private class ViewHolder {
            public AppCompatImageView icon;
            public AppCompatTextView title;
            public AppCompatTextView artist;
            public AppCompatTextView duration;
            public AppCompatTextView size;
        }
    }
}
