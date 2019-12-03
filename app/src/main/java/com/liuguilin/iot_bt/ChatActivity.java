package com.liuguilin.iot_bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.liuguilin.iot_bt.adapter.ChatListAdapter;
import com.liuguilin.iot_bt.manager.BtManager;
import com.liuguilin.iot_bt.model.ChatListModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView mChatRyView;
    private EditText et_text;
    private Button btn_send;

    private ChatListAdapter mChatListAdapter;
    private List<ChatListModel> mList = new ArrayList<>();

    // UUID，蓝牙建立链接需要的
    private final UUID MY_UUID = UUID.fromString("db764ac8-4b08-7f25-aafe-59d03c27bae3");
    // 为其链接创建一个名称
    private final String NAME = "Bluetooth_Socket";
    // 选中发送数据的蓝牙设备，全局变量，否则连接在方法执行完就结束了
    private BluetoothDevice selectDevice;
    // 获取到选中设备的客户端串口，全局变量，否则连接在方法执行完就结束了
    private BluetoothSocket clientSocket;
    // 获取到向设备写的输出流，全局变量，否则连接在方法执行完就结束了
    private OutputStream os;
    // 服务端利用线程不断接受客户端信息
    private AcceptThread thread;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String text = (String) msg.obj;
            Log.e(MainActivity.TAG, "text:" + text);
            addRight(text);
            Toast.makeText(ChatActivity.this, text, Toast.LENGTH_SHORT).show();
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initView();
    }

    private void initView() {

        mChatRyView = (RecyclerView) findViewById(R.id.mChatRyView);
        et_text = (EditText) findViewById(R.id.et_text);
        btn_send = (Button) findViewById(R.id.btn_send);

        btn_send.setOnClickListener(this);

        mChatRyView.setLayoutManager(new LinearLayoutManager(this));
        mChatListAdapter = new ChatListAdapter(this, mList);
        mChatRyView.setAdapter(mChatListAdapter);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        final String address = intent.getStringExtra("address");

        getSupportActionBar().setTitle(name);

        thread = new AcceptThread();
        thread.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                connetSocket(address);
            }
        }).start();

    }

    /**
     * 建立连接
     *
     * @param address
     */
    private void connetSocket(String address) {
        if (selectDevice == null) {
            //通过地址获取到该设备
            selectDevice = BtManager.getInstance().getRemoteDevice(address);
        }

        try {
            if (clientSocket == null) {
                // 获取到客户端接口
                clientSocket = selectDevice.createRfcommSocketToServiceRecord(MY_UUID);
                // 向服务端发送连接
                clientSocket.connect();
                // 获取到输出流，向外写数据
                os = clientSocket.getOutputStream();
            }
            sendText("连接成功");
        } catch (IOException e) {
            Log.e(MainActivity.TAG, e.toString());
        }
    }

    /**
     * 发送消息
     *
     * @param text
     */
    private void sendText(final String text) {
        if (!TextUtils.isEmpty(text)) {
            if (os != null) {
                // 以utf-8的格式发送出去
                try {
                    os.write(text.getBytes("UTF-8"));
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            addLeft(text);
                        }
                    });
                    Log.e(MainActivity.TAG, text);
                } catch (IOException e) {
                    Toast.makeText(this, "消息发送失败:" + e.toString(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "设备未连接", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                String text = et_text.getText().toString().trim();
                sendText(text);
                et_text.setText("");
                break;
        }
    }

    private class AcceptThread extends Thread {
        // 服务端接口
        private BluetoothServerSocket serverSocket;
        // 获取到客户端的接口
        private BluetoothSocket socket;
        // 获取到输入流
        private InputStream is;

        public AcceptThread() {
            serverSocket = BtManager.getInstance().listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        }

        @Override
        public void run() {
            try {
                // 接收其客户端的接口
                socket = serverSocket.accept();
                // 获取到输入流
                is = socket.getInputStream();
                while (true) {
                    // 创建一个128字节的缓冲
                    byte[] buffer = new byte[128];
                    // 每次读取128字节，并保存其读取的角标
                    int count = is.read(buffer);
                    // 创建Message类，向handler发送数据
                    Message msg = new Message();
                    // 发送一个String的数据，让他向上转型为obj类型
                    msg.obj = new String(buffer, 0, count, "utf-8");
                    // 发送数据
                    mHandler.sendMessage(msg);
                }
            } catch (IOException e) {
                Log.e(MainActivity.TAG, e.toString());
            }
        }
    }

    private void addLeft(String text) {
        ChatListModel model = new ChatListModel();
        model.setType(ChatListAdapter.LEFT_TEXT);
        model.setLeftText(text);
        mList.add(model);
        mChatListAdapter.notifyDataSetChanged();
    }

    private void addRight(String text) {
        ChatListModel model = new ChatListModel();
        model.setType(ChatListAdapter.RIGHT_TEXT);
        model.setRightText(text);
        mList.add(model);
        mChatListAdapter.notifyDataSetChanged();
    }
}
