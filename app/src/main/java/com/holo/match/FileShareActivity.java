package com.holo.match;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.holo.m.data.FileShareData;
import com.holo.m.tools.Tools;

import java.util.ArrayList;
import java.util.Map;

public class FileShareActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    ArrayList<Map<String, Object>> shareList = new ArrayList<>();
    SimpleAdapter simpleAdapter;
    ListView file_share_list;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_share);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View add_share_dialog = getLayoutInflater().inflate(R.layout.dialog_add_file_share, null);

                new AlertDialog.Builder(FileShareActivity.this)
                        .setTitle(R.string.alert_title_add_file_share)
                        .setView(add_share_dialog)
                        .setNegativeButton(R.string.alert_cancel, null)
                        .setPositiveButton(R.string.alert_add_file_share_sure, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String title = ((EditText) add_share_dialog.findViewById(R.id.file_share_title)).getText().toString();
                                String describe = ((EditText) add_share_dialog.findViewById(R.id.file_share_describe)).getText().toString();
                                if (!title.isEmpty()) {
                                    FileShareData share = new FileShareData(FileShareActivity.this);
                                    long id = share.createShareResource(title, Tools.getName(), describe);
                                    shareList.clear();
                                    share.getSharedResourceList(shareList);
                                    simpleAdapter.notifyDataSetChanged();
                                    share.close();
                                    Intent intent = new Intent(FileShareActivity.this, ShareFileDetail.class);
                                    intent.putExtra("id", id);
                                    intent.putExtra("isNew", true);
                                    startActivity(intent);
                                } else {
                                    Snackbar.make(fab, R.string.share_file_no_title, Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .show();
            }
        });

        FileShareData share = new FileShareData(this);
        share.getSharedResourceList(shareList);
        share.close();
        if (shareList.size() == 0) {
            Snackbar.make(fab, R.string.no_shared_file_found, Snackbar.LENGTH_SHORT).show();
        }
        simpleAdapter = new SimpleAdapter(this, shareList, R.layout.listview_shared_file,
                new String[]{FileShareData.RESOURCE_SHARE.TITLE, FileShareData.RESOURCE_SHARE.DESCRIBE},
                new int[]{R.id.share_file_title, R.id.share_file_describe});
        file_share_list = (ListView) findViewById(R.id.file_share_list);
        file_share_list.setAdapter(simpleAdapter);
        file_share_list.setOnItemClickListener(this);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map<String, Object> m = shareList.get(position);
        Intent intent = new Intent(this, ShareFileDetail.class);
        intent.putExtra("id", (long) m.get(FileShareData.RESOURCE_SHARE._ID));
        intent.putExtra("isNew", false);
        startActivity(intent);
    }
}
