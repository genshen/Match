package com.holo.match;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.holo.m.data.MatchDataManager;
import com.holo.m.tcp.FileReceiver;
import com.holo.m.tcp.FileSendIntentService;
import com.holo.m.tools.TimeTools;
import com.holo.m.files.FileInfo;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileRecord extends AppCompatActivity implements FileReceiver.ReceiveListener, FileSendIntentService.FileSendListener {
    Handler handler = new FileHandler();
    List<HashMap<String, Object>> file_list;
    ListView listView_file_record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_record);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FileReceiver.setReceiveListener(this);
        FileSendIntentService.setOnFileSendListener(this);

        MatchDataManager file_transfer_data = new MatchDataManager(this);
        file_list = file_transfer_data.queryAllFileTransferRecord();
        FileRecordAdapter fileRecordAdapter = new FileRecordAdapter(file_list);
        listView_file_record = (ListView) findViewById(R.id.listView_file_record);
        listView_file_record.setAdapter(fileRecordAdapter);
        listView_file_record.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openFile(file_list.get(position).get("file_name").toString(),
                        file_list.get(position).get("file_path").toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_record, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileReceiver.setReceiveListener(null);
        FileSendIntentService.setOnFileSendListener(null);
    }

    private void openFile(String file_name,String file_path) {
        String end = FileInfo.getEnd(file_name);
        File file = new File(file_path);
        String type;
        if ((type = FileInfo.matchApp(end)) != null) {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), type);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.noApplicationToOpen, Toast.LENGTH_LONG).show();
        }
    }

    //by sending a message to notify main Thread to update View,so that it can transfer the file faster!
    @Override
    public void OnReceiveStart(int id) {
        sendMessageWithData(id, 0, 0x200);
    }

    @Override
    public void OnReceiveFinish(int id, long transfer_time) {
        sendMessageWithData(id, 0, 0x201);
    }

    @Override
    public void OnReceiveError(int id) {
    }

    @Override
    public void OnSendStart(int id) {
        sendMessageWithData(id, 0, 0x100);
    }

    @Override
    public void OnSendFinish(int id, long transfer_time) {
        sendMessageWithData(id, 0, 0x101);
    }

    @Override
    public void OnSendError(int id) {
    }

    public void sendMessageWithData(int id, int transfer_time, int what) {
        Message message = new Message();
        message.what = what;
        message.arg1 = transfer_time;
        message.obj = id;
        handler.sendMessage(message);
    }

    public class FileHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x100: //send start
                    Toast.makeText(FileRecord.this, msg.obj.toString() + " file send start", Toast.LENGTH_SHORT).show();
                    break;
                case 0x101: //send finish
                    Toast.makeText(FileRecord.this, msg.obj.toString() + " file send finish", Toast.LENGTH_SHORT).show();
                    break;
                case 0x200: //receive start
                    Toast.makeText(FileRecord.this, msg.obj.toString() + " file receive start", Toast.LENGTH_SHORT).show();
                    break;
                case 0x201: //receive finish
                    Toast.makeText(FileRecord.this, msg.obj.toString() + " file receive finish", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    class FileRecordAdapter extends BaseAdapter {
        List<HashMap<String, Object>> file_list;

        public FileRecordAdapter(List<HashMap<String, Object>> file_list) {
            this.file_list = file_list;
        }

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
            return (boolean) file_list.get(position).get("sender") ? 1 : 0;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Map<String, Object> map = file_list.get(position);
            ViewHolder vh = new ViewHolder();
            if (convertView == null) {
                int layout = (boolean) map.get("sender") ? R.layout.listview_file_send : R.layout.listview_file_receive;
                convertView = getLayoutInflater().inflate(layout, parent, false);
                vh.time = (AppCompatTextView) convertView.findViewById(R.id.message_time);
                vh.file_name = (AppCompatTextView) convertView.findViewById(R.id.file_name);
                vh.file_size = (AppCompatTextView) convertView.findViewById(R.id.file_size);
                vh.transfer_time = (AppCompatTextView) convertView.findViewById(R.id.transfer_time);
                vh.file_to_who = (AppCompatTextView) convertView.findViewById(R.id.file_to_who);
                vh.file_icon = (AppCompatImageView) convertView.findViewById(R.id.file_icon);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            vh.time.setText(TimeTools.getShowAbleDate(map.get("time")));
            vh.file_name.setText((map.get("file_name")).toString());
            vh.file_size.setText(FileInfo.getFileSize((long) (map.get("file_size"))));  //Todo
            vh.transfer_time.setText((map.get("transfer_time")).toString()); //Todo
            vh.file_to_who.setText((map.get("name")).toString()); //Todo
            vh.file_icon.setImageResource(FileInfo.getFileIcon(FileInfo.getEnd(map.get("file_name").toString())));
            return convertView;
        }
    }

    class ViewHolder {
        AppCompatTextView time;
        AppCompatTextView file_name;
        AppCompatTextView file_size;
        AppCompatTextView transfer_time;
        AppCompatTextView file_to_who;
        AppCompatImageView file_icon;
    }
}
