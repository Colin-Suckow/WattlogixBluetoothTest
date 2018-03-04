package com.earthcruiser.bluetoothtest;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DeviceDetail extends AppCompatActivity {

    GenericGattCallback mGattCallback;
    BluetoothDevice mDevice;

    uuidListAdapter mAdapter;
    Button mRefreshButton;
    TextView mDeviceNameText;
    TextView mServiceFailText;
    ProgressBar mProgressBar;

    ListView uuidListView;

    Boolean foundServices;

    ArrayList<BluetoothGattService> serviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        mDeviceNameText = findViewById(R.id.deviceName);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);
        mServiceFailText = findViewById(R.id.serviceFailedText);


        serviceList = new ArrayList<>();

        mDevice = getIntent().getParcelableExtra("bluetoothDevice");
        if(mDevice.getName() != null){
            mDeviceNameText.setText(mDevice.getName());
        } else {
            mDeviceNameText.setText("Null");
        }


        mRefreshButton = findViewById(R.id.refreshButton);
        mRefreshButton.setOnClickListener(view -> refreshServices());
        mRefreshButton.setVisibility(View.GONE);

        uuidListView = findViewById(R.id.uuidListView);

        connectGatt();

        mAdapter = new uuidListAdapter(this, serviceList);
        uuidListView.setAdapter(mAdapter);
        foundServices = false;



    }

    private void connectGatt() {
        if(mDevice != null) {
            mGattCallback = new GenericGattCallback(false);
            mDevice.connectGatt(this,false,mGattCallback);
            start();

        }

    }

    private Handler handler = new Handler();

    Integer searchCount = 0;

    private Boolean refreshServices() {
        List<BluetoothGattService> mRawServices = mGattCallback.getGattServices();
        if(mRawServices.size() > 0) {
            serviceList.clear();
            for (BluetoothGattService service: mRawServices) {
                serviceList.add(service);
            }
            mAdapter.notifyDataSetChanged();
            return true;
        }

        return false;

    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            foundServices = refreshServices();
            searchCount++;
            if(foundServices) {
                stop();
                return;
            }
            if(searchCount > 7) {
                abortServiceSearch();
                stop();
                return;
            }
            start();
        }
    };

    public void stop() {
        handler.removeCallbacks(runnable);
        refreshServices();
        mProgressBar.setVisibility(View.GONE);
    }

    public void start() {

        handler.postDelayed(runnable, 1000);
    }

    private void abortServiceSearch() {
        mServiceFailText.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }
}

