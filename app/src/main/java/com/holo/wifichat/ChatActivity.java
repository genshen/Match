package com.holo.wifichat;

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
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.holo.m.data.ChatDataManager;
import com.holo.m.message.ChatMessages;
import com.holo.m.message.Messages;
import com.holo.m.tcp.FileReceiver;
import com.holo.m.tcp.FileSender;
import com.holo.m.tools.DateTools;
import com.holo.m.tools.files.BasicFileInformation;
import com.holo.m.udp.UdpReceive;
import com.holo.m.udp.UdpSend;
import com.holo.view.BubbleTextView;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements TextWatcher, UdpReceive.ReceiveChatMessageListener, View.OnFocusChangeListener {

    String title, last_ip = null, ip, mac;
    boolean editorTextEmpty = true, gridLayoutShow = false;

    EditText message_content;
    AppCompatImageButton sendButton;
    GridLayout gridLayout;
    AppCompatImageButton message_box_toggle;

    UdpSend msgSend;
    Handler handler = new MHandler();
    ChatAdapter chatAdapter;
    List<HashMap<String, Object>> chat_list;
    ChatDataManager chat_manager;

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
        Messages m = (Messages) intent.getExtras().get("m");
        getSupportActionBar().setTitle(title);
        init(m);
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        chat_manager.close();
        UdpReceive.chat_ip = last_ip;
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
                Intent intent =  new Intent(this,FileRecord.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init(Messages m) {
        if (m != null) {
            distribute(m);
        }
        UdpReceive ur = MyApp.ur;
        last_ip = UdpReceive.chat_ip;
        ur.setChatIp(ip);
        ur.setOnReceiveChatMessageListener(this);
        msgSend = new UdpSend();
        message_content = (EditText) findViewById(R.id.message_content);
        sendButton = (AppCompatImageButton) findViewById(R.id.send_message);
        gridLayout = (GridLayout) findViewById(R.id.chat_tool_box);
        message_box_toggle = (AppCompatImageButton) findViewById(R.id.message_box_toggle);

        message_content.addTextChangedListener(this);
        message_content.setOnFocusChangeListener(this);

        chat_manager = new ChatDataManager(this);
        chat_list = chat_manager.queryChatListByMac(mac);
        chatAdapter = new ChatAdapter(this, chat_list);
        ListView listview = (ListView) findViewById(R.id.message_list);
        listview.setAdapter(chatAdapter);
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
            Serializable serializable = data.getExtras().getSerializable("files");
            msgSend.sendFileRequest(ip, serializable);
            long time = DateTools.getTime();
            for (BasicFileInformation info : (List<BasicFileInformation>) serializable) {
                addNewMessage(time, ChatMessages.FILE, true, info.name, ChatMessages.SENT);
            }
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
                    long time = DateTools.getTime();
                    msgSend.sendMessage(ip, time, content);
                    chat_manager.insertContent(mac, time, ChatMessages.TEXT, 1, content, ChatMessages.SENT);
                }
                break;
            case R.id.message_content: // if it's editorText,gridLayoutShow will hidden
                if (gridLayoutShow) {
                    gridLayoutShow = false;
                    gridLayout.setVisibility(View.GONE);
                }
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
    private void addNewMessage(long time, short type, boolean sender, String content, short state) {
        HashMap<String, Object> chat = new HashMap<>();
        chat.put("time", time);
        chat.put("type", type);
        chat.put("sender", sender);
        chat.put("content", content);
        chat.put("state", state);
        chat_list.add(chat);
        chatAdapter.notifyDataSetChanged();
        chat_manager.insertContent(mac, time, type, sender ? 1 : 0, content, state);
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
            case NORMAL_MESSAGE:
                long time = messages.getTime();
                String content = (String) messages.getContent();
                addNewMessage(time, ChatMessages.TEXT, false, content, ChatMessages.RECEIVED);
                break;
            case FileSendRequest:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_file_receive_ack)
                        .setMessage(R.string.dialog_file_receive_ack_message)
                        .setPositiveButton(R.string.dialog_file_receive_ack_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int witch) {
                                List<BasicFileInformation> listFile = (List<BasicFileInformation>) messages.getContent();
                                msgSend.sendFileReply(ip, (Serializable) messages.getContent());
                                FileReceiver fileReceiver = new FileReceiver(messages.ip, (listFile).size());
                                fileReceiver.start();
                                //start file transmission and store the message to mem and database
                                for (BasicFileInformation info : listFile) {
                                    addNewMessage(messages.getTime(), ChatMessages.FILE, false, info.name, ChatMessages.RECEIVED);
                                }
                            }
                        })
                        .setNegativeButton(R.string.dialog_file_receive_ack_refuse, null).show();
                break;
            case FileSendReply:
                FileSender fileSender = new FileSender(messages.ip, (Serializable) messages.getContent());
                fileSender.start();
                break;
        }
    }


    class ChatAdapter extends BaseAdapter {
        private Context context;
        private List<HashMap<String, Object>> chat_list;
        int[] layout = {R.layout.message_send, R.layout.message_receive,
                R.layout.message_file_send, R.layout.message_file_receive};

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
            return 4;
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
                case 0: // text
                    TextViewHolder vh = (TextViewHolder) convertView.getTag();
                    vh.btv.setText(value.get("content").toString());
                    vh.time.setText(DateTools.getShowAbleDate(value.get("time")));
                    ;
                    break;
                case 1: // file
                    FileViewHolder file_view_holder = (FileViewHolder) convertView.getTag();
                    file_view_holder.file_icon.setImageResource(R.drawable.uc_file);
                    file_view_holder.file_name.setText(value.get("content").toString());
                    file_view_holder.time.setText(DateTools.getShowAbleDate(value.get("time")));
                    convertView.setTag(file_view_holder);
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
                case 0: // text
                    TextViewHolder vh = new TextViewHolder();
                    vh.btv = (BubbleTextView) convertView.findViewById(R.id.message_content);
                    vh.time = (TextView) convertView.findViewById(R.id.message_time);

                    vh.btv.setText(chat_map.get("content").toString());
                    vh.time.setText(DateTools.getShowAbleDate(chat_map.get("time")));
                    convertView.setTag(vh);
                    break;
                case 1: // file
                    FileViewHolder file_view_holder = new FileViewHolder();
                    file_view_holder.file_icon = (AppCompatImageView) convertView.findViewById(R.id.file_icon);
                    file_view_holder.file_name = (AppCompatTextView) convertView.findViewById(R.id.file_name);
                    file_view_holder.time = (TextView) convertView.findViewById(R.id.message_time);

                    file_view_holder.file_icon.setImageResource(R.drawable.uc_file);
                    file_view_holder.file_name.setText(chat_map.get("content").toString());
                    file_view_holder.time.setText(DateTools.getShowAbleDate(chat_map.get("time")));
                    convertView.setTag(file_view_holder);
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
