package com.holo.match;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.holo.m.data.MatchDataManager;
import com.holo.m.files.BasicFileInformation;
import com.holo.m.files.FileInfo;
import com.holo.m.files.FileManager;
import com.holo.m.message.ChatMessages;
import com.holo.m.message.Messages;
import com.holo.m.tcp.FileReceiver;
import com.holo.m.tcp.FileSendIntentService;
import com.holo.m.tcp.MessageSend;
import com.holo.m.tools.TimeTools;
import com.holo.m.udp.UdpReceive;
import com.holo.m.udp.UdpSend;
import com.holo.m.voice.Voice;
import com.holo.sounds.MediaRecorderDialog;
import com.holo.sounds.OnSaveButtonClickListener;
import com.holo.view.BubbleTextView;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements TextWatcher, UdpReceive.ReceiveChatMessageListener, View.OnFocusChangeListener, AdapterView.OnItemClickListener {

    String title, ip, mac;
    boolean editorTextEmpty = true, gridLayoutShow = false;

    EditText message_content;
    AppCompatImageButton sendButton;
    GridLayout gridLayout;
    AppCompatImageButton message_box_toggle;

    UdpSend msgSend;
    Handler handler = new MHandler();
    ChatAdapter chatAdapter;
    List<HashMap<String, Object>> chat_list;
    MatchDataManager chat_manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        title = intent.getStringExtra("name");
        ip = intent.getStringExtra("ip");
        mac = intent.getStringExtra("mac");
        Messages m = (Messages) intent.getSerializableExtra("m");
        getSupportActionBar().setTitle(title);
        init(m);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!chat_manager.isOpen()) { // re open db
            chat_manager = new MatchDataManager(this);
        }
        chatAdapter.notifyDataSetChanged();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        chat_manager.close();
        UdpReceive.chat_ip = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
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
            case android.R.id.home:
                end();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        end();
    }

    private void end() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void init(Messages m) {
        if (m != null) {
            distribute(m);
        }
        UdpReceive ur = MyApp.ur;
        ur.setChatIp(ip);
        ur.setOnReceiveChatMessageListener(this);
        msgSend = new UdpSend();
        message_content = (EditText) findViewById(R.id.message_content);
        sendButton = (AppCompatImageButton) findViewById(R.id.send_message);
        gridLayout = (GridLayout) findViewById(R.id.chat_tool_box);
        message_box_toggle = (AppCompatImageButton) findViewById(R.id.message_box_toggle);

        message_content.addTextChangedListener(this);
        message_content.setOnFocusChangeListener(this);

        chat_manager = new MatchDataManager(this);
        chat_list = chat_manager.queryChatListByMac(mac);
        chatAdapter = new ChatAdapter(this, chat_list);
        ListView listview = (ListView) findViewById(R.id.message_list);
        listview.setAdapter(chatAdapter);
        listview.setOnItemClickListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!message_content.getText().toString().isEmpty()) {
            if (!editorTextEmpty) return;
            editorTextEmpty = false;
            sendButton.setImageResource(R.drawable.ic_send);
        } else {
            sendButton.setImageResource(R.drawable.ic_send_gray);
            editorTextEmpty = true;
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (gridLayoutShow) {
            gridLayoutShow = false;
            gridLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x200 && resultCode == RESULT_OK) {
            List<BasicFileInformation> listInfo = (List<BasicFileInformation>) data.getSerializableExtra("files");
            if (listInfo == null) return; // listInfo may cause nullPointer.
            long time = TimeTools.getTime();
            for (BasicFileInformation info : listInfo) {
                //Todo now ,transfer_time = 0;file_type = 1,file_state = 0,remark = ""
                info.sender_id = chat_manager.insertFileTransferRecord(mac, time, title,
                        1, 1, 1, info.size, info.name, info.path, 0, "");
                addNewMessage(time, ChatMessages.FILE, true, info.name, info.size, ChatMessages.SENT);
            }
            msgSend.sendFileRequest(ip, (Serializable) listInfo);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String, Object> chat = chat_list.get(position);
        switch ((short) chat.get("type")) {
            case ChatMessages.TEXT:
                break;
            case ChatMessages.FILE:
                break;
            case ChatMessages.VOICE:
                Voice.playVoice(this, chat_list.get(position).get("content").toString());
                break;
        }
    }

    public void ChatClick(View v) {
        switch (v.getId()) {
            case R.id.message_box_toggle: //hide input method first
                HideInputMethod();
                if (gridLayoutShow) {
                    gridLayoutShow = false;
                    gridLayout.setVisibility(View.GONE);
                } else {
                    gridLayoutShow = true;
                    gridLayout.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.send_message:
                String content = message_content.getText().toString();
                if (!content.isEmpty()) {
                    long time = TimeTools.getTime();
                    msgSend.sendMessage(ip, time, content);
                    addNewMessage(time, ChatMessages.TEXT, true, content, 0, ChatMessages.SENT);
                    message_content.setText(""); // empty text
                }
                break;
            case R.id.message_content: // if it's editorText,gridLayoutShow will hidden
                if (gridLayoutShow) {
                    gridLayoutShow = false;
                    gridLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.tool_box_voice:
                new MediaRecorderDialog.Builder(this, Voice.BASE_PATH)
                        .setOutputFormat(MediaRecorderDialog.OutputFormat.MPEG_4)
                        .setAudioEncoder(MediaRecorderDialog.AudioEncoder.AAC)
                        .setTitle("Recording,,,")
                        .setMessage("Press the button")
                        .setOnSaveButtonClickListener(new OnSaveButtonClickListener() {
                            @Override
                            public void onSucceed(String file_name, int duration) {
                                long time = TimeTools.getTime();
                                addNewMessage(time, ChatMessages.VOICE, true, file_name, (long) duration, ChatMessages.RECEIVED);
                                msgSend.sendVoiceRequest(ip, time, duration, file_name);
                            }

                            @Override
                            public void onFailure() {
                            }
                        })
                        .show();
                break;
            case R.id.tool_box_send_file:
                Intent fileSelector = new Intent(this, FileSelector.class);
                startActivityForResult(fileSelector, 0x200);
                break;
        }
    }

    /**
     * @param time    the time when receive message
     * @param type    0 for text, 1 for file, 2 for image...
     * @param sender  true for sender(should be displayed at the right side), false for receiver (left side)
     * @param content text message or file name
     * @param state   (for a {@code sender == true} message) 0 for sent(may lost) ,1 for received,2 for read
     */
    private void addNewMessage(long time, short type, boolean sender, String content, long length, short state) {
        HashMap<String, Object> chat = new HashMap<>();
        chat.put("time", time);
        chat.put("type", type);
        chat.put("sender", sender);
        chat.put("content", content);
        chat.put("length", length);
        chat.put("state", state);
        chat_list.add(chat);
        chatAdapter.notifyDataSetChanged();
        chat_manager.insertChatRecord(mac, time, type, sender ? 1 : 0, content, length, state);
    }

    private void HideInputMethod() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onReceiveThisChatMessage(Messages msg) {
        Message message = new Message();
        message.what = 0x100;
        message.obj = msg;
        handler.sendMessage(message);
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
                    distribute(messages);
                    break;
            }
        }
    }

    private void distribute(final Messages messages) {
        switch (messages.type) {
            case TEXT_MESSAGE:
                long time = messages.getTime();
                String content = (String) messages.getContent();
                addNewMessage(time, ChatMessages.TEXT, false, content, 0, ChatMessages.RECEIVED);
                break;
            case VOICE_MESSAGE_Request:
                // start receive message;
                long voice_time = messages.getTime();
                addNewMessage(voice_time, ChatMessages.VOICE, false, (String) messages.getContent(),
                        messages.getLength(), ChatMessages.RECEIVED);
                msgSend.sendVoiceReply(messages.ip, (String) messages.getContent());
                break;
            case VOICE_MESSAGE_Reply:
                MessageSend voice = new MessageSend(messages.ip, (String) messages.getContent());
                voice.start();
                break;
            case FileSendRequest:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_file_receive_ack)
                        .setMessage(R.string.dialog_file_receive_ack_message)
                        .setPositiveButton(R.string.dialog_file_receive_ack_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int witch) {
                                List<BasicFileInformation> listFile = (List<BasicFileInformation>) messages.getContent();
                                FileReceiver fileReceiver = new FileReceiver(messages.ip, (listFile).size());
                                fileReceiver.start();
                                //start file transmission and store the message to mem and database
                                String base_store_path = FileManager.getSDPath() + "/";
                                for (BasicFileInformation info : listFile) {
                                    addNewMessage(messages.getTime(), ChatMessages.FILE, false, info.name, info.size, ChatMessages.RECEIVED);
                                    //Todo now ,transfer_time = 0;file_type = 1,remark = "",file_state = 0
                                    info.receive_id = chat_manager.insertFileTransferRecord(messages.mac, messages.getTime(), messages.name,
                                            0, 0, 1, info.size, info.name, base_store_path + info.name, 0, "");
                                }
                                msgSend.sendFileReply(ip, (Serializable) listFile);
                            }
                        })
                        .setNegativeButton(R.string.dialog_file_receive_ack_refuse, null).show();
                break;
            case FileSendReply:
                Intent intent = new Intent(this, FileSendIntentService.class);
                Bundle b = new Bundle();
                b.putSerializable("files", (Serializable) messages.getContent());
                intent.putExtra("ip", ip);
                intent.putExtras(b);
                startService(intent);
//                FileSender fileSender = new FileSender(messages.ip, (Serializable) messages.getContent());
//                fileSender.start();
                break;
        }
    }


    class ChatAdapter extends BaseAdapter {
        private Context context;
        private List<HashMap<String, Object>> chat_list;
        int[] layout = {R.layout.message_send, R.layout.message_receive,
                R.layout.message_file_send, R.layout.message_file_receive,
                R.layout.message_voice_send, R.layout.message_voice_receive};

        public ChatAdapter(Context context, List<HashMap<String, Object>> chat_list) {
            this.context = context;
            this.chat_list = chat_list;
        }

        @Override
        public int getCount() {
            // How many items are in the data set represented by this Adapter.
            return chat_list.size();
        }

        @Override
        public Object getItem(int position) {
            // Get the data item associated with the specified position in the data set.
            return null;
        }

        @Override
        public long getItemId(int position) {
            // Get the row id associated with the specified position in the list.
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            Map<String, Object> m = chat_list.get(position);
            return getViewType((boolean) m.get("sender"), (short) m.get("type"));
        }

        @Override
        public int getViewTypeCount() {
            return 6;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get a View that displays the data at the specified position in the data set.
            Map<String, Object> chat_map = chat_list.get(position);
            if (convertView == null) {
                convertView = getNewView(chat_map, layout, parent);
            } else {
                setView(convertView, chat_map);
            }
            return convertView;
        }

        private void setView(View convertView, Map<String, Object> value) {
            short type = (short) value.get("type");
            switch (type) {
                case ChatMessages.TEXT: // text
                    TextViewHolder vh = (TextViewHolder) convertView.getTag();
                    vh.btv.setText(value.get("content").toString());
                    vh.time.setText(TimeTools.getShowAbleDate(value.get("time")));
                    break;
                case ChatMessages.FILE: // file
                    FileViewHolder file_view_holder = (FileViewHolder) convertView.getTag();
                    file_view_holder.file_icon.setImageResource(
                            FileInfo.getFileIcon(FileInfo.getEnd(value.get("content").toString())));
                    file_view_holder.file_name.setText(value.get("content").toString());
                    file_view_holder.time.setText(TimeTools.getShowAbleDate(value.get("time")));
                    convertView.setTag(file_view_holder);
                    break;
                case ChatMessages.VOICE:
                    TextViewHolder voice_view = (TextViewHolder) convertView.getTag();
                    voice_view.btv.setText(TimeTools.getVoiceDurationDisplay((long) value.get("length")));
                    voice_view.time.setText(TimeTools.getShowAbleDate(value.get("time")));
                    break;
            }
        }

        private View getNewView(Map<String, Object> chat_map, int[] layout, ViewGroup parent) {
            View convertView;
            short type = (short) chat_map.get("type");
            boolean sender = (boolean) chat_map.get("sender");
            // select layout from array layout[],getViewType(sender, type) returns the array index
            convertView = getLayoutInflater().inflate(layout[getViewType(sender, type)], parent, false);
            // set viewHolder and value for each case
            switch (type) {
                case ChatMessages.TEXT: // text
                    TextViewHolder vh = new TextViewHolder();
                    vh.btv = (BubbleTextView) convertView.findViewById(R.id.message_content);
                    vh.time = (TextView) convertView.findViewById(R.id.message_time);

                    vh.btv.setText(chat_map.get("content").toString());
                    vh.time.setText(TimeTools.getShowAbleDate(chat_map.get("time")));
                    convertView.setTag(vh);
                    break;
                case ChatMessages.FILE: // file
                    FileViewHolder file_view_holder = new FileViewHolder();
                    file_view_holder.file_icon = (AppCompatImageView) convertView.findViewById(R.id.file_icon);
                    file_view_holder.file_name = (AppCompatTextView) convertView.findViewById(R.id.file_name);
                    file_view_holder.time = (TextView) convertView.findViewById(R.id.message_time);

                    file_view_holder.file_icon.setImageResource(
                            FileInfo.getFileIcon(FileInfo.getEnd(chat_map.get("content").toString()))); // todo 文件类型可以存数据库
                    file_view_holder.file_name.setText(chat_map.get("content").toString());
                    file_view_holder.time.setText(TimeTools.getShowAbleDate(chat_map.get("time")));
                    convertView.setTag(file_view_holder);
                    break;
                case ChatMessages.VOICE:
                    TextViewHolder voice_view = new TextViewHolder();
                    voice_view.btv = (BubbleTextView) convertView.findViewById(R.id.message_content);
                    voice_view.time = (TextView) convertView.findViewById(R.id.message_time);

                    voice_view.btv.setText(TimeTools.getVoiceDurationDisplay((long) chat_map.get("length")));
                    voice_view.time.setText(TimeTools.getShowAbleDate(chat_map.get("time")));
                    convertView.setTag(voice_view);
                    break;
            }
            return convertView;
        }
    }

    class TextViewHolder {
        TextView time;
        BubbleTextView btv;
    }

    class FileViewHolder {
        TextView time;
        ImageView file_icon;
        TextView file_name;
    }

    /**
     * @param sender map.get("sender"), which map is one of chat_list
     * @param type   map.get("type"), which map is one of chat_list
     * @return the different return value means different view
     */
    public static int getViewType(boolean sender, short type) {
        return 2 * type + (sender ? 0 : 1);
    }
}
