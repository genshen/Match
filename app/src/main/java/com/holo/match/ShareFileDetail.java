package com.holo.match;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.holo.m.data.FileShareData;
import com.holo.m.files.BasicFileInformation;
import com.holo.m.files.FileInfo;
import com.holo.m.tools.TimeTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShareFileDetail extends AppCompatActivity {
    ArrayList<Map<String, Object>> shareFileList = new ArrayList<>();
    SharedFileAdapter sharedFileAdapter;
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_file_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShareFileDetail.this, ShareFileAddActivity.class);
                startActivityForResult(intent, 200);
            }
        });
        init(fab);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK) {
            List<BasicFileInformation> listInfo = (List<BasicFileInformation>) data.getSerializableExtra("files");
            if (listInfo == null) return; // listInfo may cause nullPointer.
            FileShareData share = new FileShareData(this);
            share.addSharedFiles(id, listInfo);
            shareFileList.clear();
            share.getSharedFileList(shareFileList, id); // refresh
            sharedFileAdapter.notifyDataSetChanged();
            share.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init(View fab) {
        id = getIntent().getLongExtra("id", 0);
        boolean isNew = getIntent().getBooleanExtra("isNew", false);
        if (isNew) {
            Snackbar.make(fab, R.string.share_file_detail_new, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        // set title
        FileShareData share = new FileShareData(this);
        Map<String, Object> resourceData = share.getSharedResourceById(id);
        share.getSharedFileList(shareFileList, id);
        share.close();
        ActionBar actionbar = getSupportActionBar();
        if (resourceData != null && actionbar != null && resourceData.size() != 0) {
            getSupportActionBar().setTitle(resourceData.get(FileShareData.RESOURCE_SHARE.TITLE).toString());
            getSupportActionBar().setSubtitle(resourceData.get(FileShareData.RESOURCE_SHARE.DESCRIBE).toString());
        } else {
            Snackbar.make(fab, R.string.share_resource_not_found, Snackbar.LENGTH_LONG).show();
            return;
        }

        // set file list
        sharedFileAdapter = new SharedFileAdapter();
        ListView sharedFileList = (ListView) findViewById(R.id.shared_file_list);
        sharedFileList.setAdapter(sharedFileAdapter);
    }

    private class SharedFileAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return shareFileList.size();
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
            Map<String, Object> m = shareFileList.get(position);
            FileView dirView;
            if (convertView == null) {
                dirView = new FileView();
                convertView = getLayoutInflater().inflate(R.layout.listview_file_selector_file, null);
                dirView.name = (AppCompatTextView) convertView.findViewById(R.id.file_selector_title);
                dirView.icon = (AppCompatImageView) convertView.findViewById(R.id.file_selector_icon);
                dirView.size = (AppCompatTextView) convertView.findViewById(R.id.file_selector_size);
                dirView.modify_time = (AppCompatTextView) convertView.findViewById(R.id.file_selector_modify_time);
                convertView.setTag(dirView);
            } else {
                dirView = (FileView) convertView.getTag();
            }
            String file_name = m.get(FileShareData.SHARE_DETAIL.TITLE).toString();
            dirView.name.setText(file_name);
            dirView.icon.setImageResource(FileInfo.getFileIcon(FileInfo.getEnd(file_name)));
            dirView.size.setText(FileInfo.getFileSize((long) m.get(FileShareData.SHARE_DETAIL.FILE_SIZE)));
            dirView.modify_time.setText(TimeTools.getShowAbleDate((long) m.get(FileShareData.SHARE_DETAIL.TIME) * 1000));
            return convertView;
        }
    }

    class FileView {
        AppCompatTextView name;
        AppCompatTextView size;
        AppCompatTextView modify_time;
        AppCompatImageView icon;
    }
}
