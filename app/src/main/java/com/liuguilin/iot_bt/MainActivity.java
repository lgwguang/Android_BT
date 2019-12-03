package com.liuguilin.iot_bt;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.liuguilin.iot_bt.adapter.BtListAdapter;
import com.liuguilin.iot_bt.manager.BtManager;
import com.liuguilin.iot_bt.model.BtListModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "IOT_BT";

    public static final int PERMISSION_REQUEST_COARSE_LOCATION = 1001;

    private RecyclerView mBtListRyView;
    private BtListAdapter mBtListAdapter;
    private List<BtListModel> mList = new ArrayList<>();

    private BtFoundReceiver mBtFoundReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mBtListRyView = (RecyclerView) findViewById(R.id.mBtListRyView);
        mBtListRyView.setLayoutManager(new LinearLayoutManager(this));
        mBtListAdapter = new BtListAdapter(this, mList);
        mBtListRyView.setAdapter(mBtListAdapter);

        mBtListAdapter.setOnItemClickListener(new BtListAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int i) {
                BtManager.getInstance().cancelDiscovery();
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                BtListModel model = mList.get(i);
                intent.putExtra("name", model.getName());
                intent.putExtra("address", model.getAddress());
                Log.i(MainActivity.TAG,"info:"+model.getName()+"\t"+model.getAddress());
                startActivity(intent);
            }
        });

        mBtFoundReceiver = new BtFoundReceiver();
        IntentFilter filter = new IntentFilter();
        //搜索结果
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        //搜索完成
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mBtFoundReceiver, filter);

        if (!BtManager.getInstance().isSupport()) {
            Toast.makeText(this, "当前设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }

        BtManager.getInstance().open();

        Set<BluetoothDevice> mBondedList = BtManager.getInstance().getBondedDevices();
        for (BluetoothDevice device : mBondedList) {
            addModel(device.getName(), device.getAddress());
        }

        requestPermission();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            } else {
                startDiscovery();
            }
        } else {
            startDiscovery();
        }
    }

    private void startDiscovery() {
        BtManager.getInstance().startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBtFoundReceiver);
    }

    class BtFoundReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    //判断是否配对过
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        addModel(device.getName(), device.getAddress());
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

            }
        }
    }

    /**
     * 添加数据
     *
     * @param name
     * @param address
     */
    private void addModel(String name, String address) {
        Log.e(TAG, "name:" + name + "address:" + address);

        if (TextUtils.isEmpty(name)) {
            return;
        }

        if (TextUtils.isEmpty(address)) {
            return;
        }

        boolean isAdd = false;
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).getAddress().equals(address)) {
                isAdd = true;
            }
        }

        if (isAdd) {
            return;
        }

        BtListModel model = new BtListModel();
        model.setName(name);
        model.setAddress(address);
        mList.add(model);
        mBtListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                int result = grantResults[i];
                if (result == PackageManager.PERMISSION_GRANTED) {
                    startDiscovery();
                } else {
                    requestPermission();
                }
            }
        }
    }
}
