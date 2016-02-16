package com.holo.match.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.holo.m.files.BasicFileInformation;
import com.holo.m.tools.TimeTools;
import com.holo.m.files.FileInfo;
import com.holo.m.files.FileManager;
import com.holo.match.R;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSelectorFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnKeyListener, AbsListView.MultiChoiceModeListener {
    final String NAME = "name";
    final String SIZE = "size";
    final String LAST_MODIFY = "last_modify";
    final String IS_DIR = "is_dir";
    List<Map<String, Object>> file_list;
    AppCompatTextView file_path_text_view;
    ListView listView;
    FileAdapter fileAdapter;
    String current_dir = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        current_dir = FileManager.getSDPath();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_file_selector, container, false); //recycle music selector layout

        File root = new File(current_dir);
        file_list = getFileChildren(root);
        file_path_text_view = (AppCompatTextView) rootView.findViewById(R.id.file_slelctor_path);
        file_path_text_view.setText(current_dir);

        fileAdapter = new FileAdapter();
        listView = (ListView) rootView.findViewById(R.id.file_selector_listview);   //recycle music selector layout
        listView.setAdapter(fileAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnKeyListener(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(this);
        return rootView;
    }

    public List<Map<String, Object>> getFileChildren(File dir) {
        List<Map<String, Object>> fileList = new ArrayList<>();
        File[] children = dir.listFiles();
        for (File child : children) {
            if (child.isHidden()) continue;            // don't show hidden dir and file;
            Map<String, Object> map = new HashMap<>();
            map.put(NAME, child.getName());
            boolean is_dir = child.isDirectory();
            map.put(IS_DIR, is_dir);
            if (!is_dir) {
                map.put(SIZE, child.length());
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

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            File f = new File(current_dir);
            if (f.getPath().equals(FileManager.getSDPath())) {
                return false;
            }
            f = f.getParentFile();
            current_dir = f.getPath();
            file_list = getFileChildren(f);
            fileAdapter.notifyDataSetChanged();
            file_path_text_view.setText(current_dir);
            return true;
        }
        return false;
    }


    private void startResult(List<BasicFileInformation> files) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("files", (Serializable) files);
        intent.putExtras(bundle);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Map<String, Object> m = file_list.get(position);
        if ((boolean) m.get(IS_DIR)) {
            String temp_dir_name = current_dir + ("/" + m.get(NAME));
            List<Map<String, Object>> fl = getFileChildren(new File(temp_dir_name));
            if (fl.size() == 0) {  //empty dir
                Toast.makeText(getContext(), R.string.empty_dir, Toast.LENGTH_SHORT).show();
                return;
            }
            current_dir = temp_dir_name;
            file_list = fl;
            fileAdapter.notifyDataSetChanged();
            file_path_text_view.setText(current_dir);
//            FIle
        } else {  //send this file
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.dialog_file_selector_send_ack)
                    .setMessage(R.string.dialog_file_selector_send_ack_message)
                    .setPositiveButton(R.string.dialog_file_selector_send_ack_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int witch) {
                            List<BasicFileInformation> music = new ArrayList<>();
                            music.add(new BasicFileInformation(current_dir + ("/" + m.get(NAME))));
                            startResult(music);
                        }
                    })
                    .setNegativeButton(R.string.dialog_file_selector_send_ack_cancel, null).show();
        }
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        if(checked && (boolean)file_list.get(position).get(IS_DIR)){
            listView.setItemChecked(position,false);
            return;
        }
        mode.setTitle(getString(R.string.have_selected_count, listView.getCheckedItemCount()));
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.file_selector_app, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        mode.setTitle(getString(R.string.have_selected_count, listView.getCheckedItemCount()));
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_send:
                List<BasicFileInformation> files = new ArrayList<>();
                for (int i = fileAdapter.getCount() - 1; i >= 0; i--) {
                    if (listView.isItemChecked(i)) {
                        final Map<String, Object> m = file_list.get(i);
                        files.add(new BasicFileInformation(current_dir + ("/" + m.get(NAME))));
                    }
                }
                startResult(files);
                return true;
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        for (int i = fileAdapter.getCount() - 1; i >= 0; i--) {
            listView.setItemChecked(i, false);
        }
    }

    private class FileAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return file_list.size();
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
        public int getItemViewType(int position) {
            Map<String, Object> m = file_list.get(position);
            return (boolean) m.get(IS_DIR) ? 0 : 1;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Map<String, Object> m = file_list.get(position);
            boolean is_dir = (boolean) m.get(IS_DIR);
            String file_name = m.get(NAME).toString();
            DirView dirView;
            if (convertView == null) {
                dirView = new DirView();
                convertView = getLayoutInflater(null).inflate(
                        is_dir ? R.layout.listview_file_selector_dir : R.layout.listview_file_selector_file, null);
                dirView.name = (AppCompatTextView) convertView.findViewById(R.id.file_selector_title);
                if (!is_dir) {
                    dirView.icon = (AppCompatImageView) convertView.findViewById(R.id.file_selector_icon);
                    dirView.size = (AppCompatTextView) convertView.findViewById(R.id.file_selector_size);
                    dirView.modify_time = (AppCompatTextView) convertView.findViewById(R.id.file_selector_modify_time);
                }
                convertView.setTag(dirView);
            } else {
                dirView = (DirView) convertView.getTag();
            }
            dirView.name.setText(file_name);
            if (!is_dir) {
                dirView.icon.setImageResource(FileInfo.getFileIcon(FileInfo.getEnd(file_name)));
                dirView.size.setText(FileInfo.getFileSize((long) m.get(SIZE)));
                dirView.modify_time.setText(TimeTools.getShowAbleDate(m.get(LAST_MODIFY)));
            }
            return convertView;
        }
    }

    class DirView {
        AppCompatTextView name;
        AppCompatTextView size;
        AppCompatTextView modify_time;
        AppCompatImageView icon;
    }
}
