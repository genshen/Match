package com.holo.match;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.github.clans.fab.FloatingActionButton;
import com.holo.m.message.Messages;
import com.holo.m.tcp.FileSendIntentService;
import com.holo.m.tcp.MessageReceiverService;
import com.holo.m.tools.Tools;
import com.holo.m.udp.UdpReceive;
import com.holo.m.udp.UdpSend;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, UdpReceive.ReceiveMessageListener {

    ListView chat_list;
    SimpleAdapter chat_sa;
    List<Map<String, Object>> friend_lsit;

    static final int NOTIFICATION_ID = 0x123;
    NotificationManager nm;

    UdpSend msgSend;
    UdpReceive ur;
    Intent voice_receive_intent;
    Handler handler = new MHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.float_action_item_computer);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebActivity.class);
                startActivity(intent);
            }
        });
        FloatingActionButton fab_share = (FloatingActionButton) findViewById(R.id.float_action_item_file_share);
        fab_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FileShareActivity.class);
                startActivity(intent);
            }
        });

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.file_record:
                Intent intent = new Intent(this, FileRecord.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ur.close();
        stopService(voice_receive_intent);
    }

    private void init() {
        chat_list = (ListView) findViewById(R.id.chat_list);
        friend_lsit = new ArrayList<>();
        friend_lsit.add(getSelfInfo());
        chat_sa = new SimpleAdapter(this, friend_lsit, R.layout.listview_friends,
                new String[]{"user_name", "ip", "message_point"},
                new int[]{R.id.user_name, R.id.user_ip, R.id.message_point});
        chat_list.setAdapter(chat_sa);
        chat_list.setOnItemClickListener(this);
        msgSend = new UdpSend();
        msgSend.online();

        ur = new UdpReceive();
        MyApp.ur = ur;
        ur.setOnReceiveMessageListener(this);
        ur.start();

        voice_receive_intent = new Intent(this, MessageReceiverService.class);
        startService(voice_receive_intent);
    }

    private Map<String, Object> getSelfInfo() {
        Map<String, Object> selfInfo = new HashMap<>();
        selfInfo.put("user_name", Tools.getName() + "（自己）");
        selfInfo.put("ip", Tools.getLocalHostIp());
        selfInfo.put("mac", MyApp.mac);
        selfInfo.put("message_point", "0");
        return selfInfo;
    }

    @Override
    public void onReceiveMessage(Messages msg) {
        Message message = new Message();
        message.what = 0x100;
        message.obj = msg;
        handler.sendMessage(message);
    }

//    private void processMultiCast(Messages msg) {
//        switch (msg.type) {
//            case ONLINE:
//                checkUser(msg.mac, msg.ip, msg.name, false);
//                msgSend.onlineReply(msg.ip);
//                break;
//        }
//    }

    private void distributeMessage(Messages msg) {
        switch (msg.type) {
            case ONLINE_REPLY:
                checkUser(msg.mac, msg.ip, msg.name, false);
                break;
            case ONLINE:
                checkUser(msg.mac, msg.ip, msg.name, false);
                msgSend.onlineReply(msg.ip);
                break;
            case TEXT_MESSAGE:
                checkUser(msg.mac, msg.ip, msg.name, true);
                setUnread(msg.ip, msg.name);
                break;
            case VOICE_MESSAGE_Request:
//                setUnread(msg.ip, msg.name);
                break;
            case FileSendRequest:
                // dialog! default answer is agree!
                Intent chat_intent = new Intent(this, ChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("m", msg);
                chat_intent.putExtras(bundle);
                chat_intent.putExtra("name", msg.name);
                chat_intent.putExtra("ip", msg.ip);
                chat_intent.putExtra("mac", msg.mac);

                PendingIntent pi = PendingIntent.getActivity(this, 0, chat_intent, 0);

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.notice_file_receive_title))
                        .setTicker(getString(R.string.notice_file_receive_title))
                        .setContentText(getString(R.string.notice_file_receive_content, msg.name))
                        .setContentIntent(pi)
                        .setAutoCancel(true);
                nm.notify(NOTIFICATION_ID, mBuilder.build());
//                may cause:  Parcelable encountered IOException reading a Serializable object
                break;
            case FileSendReply:
//                FileSender fileSender = new FileSender(msg.ip, (Serializable) msg.getContent());
//                fileSender.start();
                Intent intent = new Intent(this, FileSendIntentService.class);
                Bundle b = new Bundle();
                b.putSerializable("files", (Serializable) msg.getContent());
                intent.putExtras(b);
                intent.putExtra("ip", msg.ip);
                startService(intent);
                break;
        }
    }

    private void checkUser(String mac, String ip, String name, boolean add) {
        for (Map<String, Object> info : friend_lsit) {
            if (info.get("mac").equals(mac)) {    //更新
                info.put("user_name", name);
                info.put("ip", ip);
                if (add)
                    info.put("message_point", (Integer.parseInt((String) info.get("message_point")) + 1) + "");
                chat_sa.notifyDataSetChanged();
                return;
            }
        }
        //新增
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("user_name", name);
        userInfo.put("ip", ip);
        userInfo.put("mac", mac);
        userInfo.put("message_point", "0");
        friend_lsit.add(userInfo);
        chat_sa.notifyDataSetChanged();
    }

    private void setUnread(String ip, String name) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map<String, Object> m = friend_lsit.get(position);
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("name", (String) m.get("user_name"));
        intent.putExtra("ip", (String) m.get("ip"));
        intent.putExtra("mac", (String) m.get("mac"));
        intent.putExtra("message_point", (String) m.get("message_point"));
        startActivity(intent);
    }

    /**
     * it should be static, but we can't
     */
    public class MHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x100:
                    Messages messages = (Messages) msg.obj;
                    distributeMessage(messages);
                    break;
            }
        }
    }

}
