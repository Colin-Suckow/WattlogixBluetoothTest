package com.earthcruiser.bluetoothtest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;




public class MainActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter;

    private boolean mScanning;
    private int SCAN_PERIOD = 5000;

    static final int REQUEST_ENABLE_BT = 601;
    static final int REQUEST_FINE_LOCATION = 602;

    public HashMap<String, BluetoothDevice> mScanResults;

    BluetoothLeScanner mBluetoothLeScanner;
    BtleScanCallback mScanCallback;
    Handler mHandler;

    BluetoothListAdapter adapter;

    ArrayList<BluetoothDevice> deviceList;

    Button scanButton;
    ListView deviceListView;
    ProgressBar progressBar;

    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceListView = (ListView) findViewById(R.id.bluetoothList);

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object object = deviceListView.getItemAtPosition(i);
                BluetoothDevice device = (BluetoothDevice) object;
                Intent detailActivityIntent;
                if(device.getName() != null && device.getName().equals("Project Zero R2")) {
                    detailActivityIntent = new Intent(context, ProjectZeroActivity.class);
                } else {
                    detailActivityIntent = new Intent(context, DeviceDetail.class);
                }
                detailActivityIntent.putExtra("bluetoothDevice", device);
                startActivity(detailActivityIntent);
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.scanProgressBar);

        scanButton = (Button) findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"Starting Scan");
                startScan();
            }
        });

        deviceList = new ArrayList<>();

        adapter = new BluetoothListAdapter(this, deviceList);
        deviceListView.setAdapter(adapter);

        mScanResults = new HashMap();
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mScanning = false;
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!getPackageManager().hasSystemFeature((PackageManager.FEATURE_BLUETOOTH_LE))) {
            finish();
        }
    }

    private void startScan() {
        if (!hasPermissions() || mScanning) {
            Log.e(TAG,"Failed Permission Check");
            return;
        }

        List<ScanFilter> filters = new ArrayList<>();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();
        mScanCallback = new BtleScanCallback();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        progressBar.setVisibility(View.VISIBLE);
        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
        mScanning = true;
        mHandler = new Handler();
        Log.d(TAG,"======== got to this point in scanning");
        mHandler.postDelayed(this::stopScan, SCAN_PERIOD);

    }

    private void stopScan() {
        Log.d(TAG,"The scan actually stopped");
        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
            scanComplete();
        }

        mScanCallback = null;
        mScanning = false;
        mHandler = null;

    }

    private void scanComplete() {
        progressBar.setVisibility(View.GONE);
        Log.d(TAG,"Scan Complete");
        if (mScanResults.isEmpty()) {
            return;
        }

        deviceList.clear();

        for (Object key : mScanResults.keySet()){
            Log.d(TAG,buildDeviceLog(mScanResults.get(key)));
            deviceList.add((mScanResults.get(key)));
        }

        //adapter.clear();
        adapter.notifyDataSetChanged();


    }


    private class BtleScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addScanResult(result);
        }

        public void onBatchScanResult(List<ScanResult> results) {
            for (ScanResult result : results) {
                addScanResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "BLE Scan Failed with code " + errorCode);
        }

        private void addScanResult(ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String deviceAddress = device.getAddress();
            mScanResults.put(deviceAddress,device);
        }

    }



    private boolean hasPermissions() {

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.d(TAG,"Requesting Bluetooth");
            requestBluetoothEnable();
            return false;
        } else if (!hasLocationPermissions()) {
            Log.d(TAG,"Requesting Location");
            requestLocationPermission();
            return false;
        }
        Log.d(TAG,"Not requesting anything");
        return true;
    }

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private boolean hasLocationPermissions() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }

    private String buildDeviceLog(BluetoothDevice device) {
        return "\n" +
                "========Found Device=======\n" +
                "Name: " + device.getName() + "\n" +
                "Address: " + device.getAddress() + "\n";


    }

}

