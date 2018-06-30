package com.earthcruiser.bluetoothtest;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class ProjectZeroActivity extends AppCompatActivity {

    BluetoothDevice mDevice;

    ZeroGattCallback mGattCallback;

    ArrayList<BluetoothGattService> serviceList;

    BluetoothGattService buttonService;
    BluetoothGattService dataService;

    BluetoothGattCharacteristic button0Char;
    BluetoothGattCharacteristic button1Char;
    BluetoothGattCharacteristic stringChar;

    ArrayList<BluetoothGattCharacteristic> characteristics;
    ArrayList<Float> voltageList;


    boolean gattConnected = false;

    BluetoothGatt mGatt;

    String vData = "No Data";

    TextView mServicesText;
    TextView mButtonZeroStateText;
    TextView mButtonOneStateText;
    TextView vDataText;
    boolean mStopHandler = false;

    BarChart chart;

    Integer updateCounter = 0;

    private static final UUID BUTTON_SERVICE_UUID = UUID.fromString("F0001120-0451-4000-B000-000000000000");
    private static final UUID BUTTON_0_UUID = UUID.fromString("F0001121-0451-4000-B000-000000000000");
    private static final UUID BUTTON_1_UUID = UUID.fromString("F0001122-0451-4000-B000-000000000000");

    private static final UUID DATA_SERVICE_UUID = UUID.fromString("F0001130-0451-4000-B000-000000000000");
    private static final UUID STRING_UUID = UUID.fromString("F0001131-0451-4000-B000-000000000000");

    public final static UUID UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_zero);
        mServicesText = findViewById(R.id.mServicesText);
        mButtonZeroStateText = findViewById(R.id.buttonZeroStateText);
        mButtonOneStateText = findViewById(R.id.buttonOneStateText);
        mButtonOneStateText.setKeepScreenOn(true);

        chart = (BarChart) findViewById(R.id.chart);
        chart.getAxisLeft().setAxisMinimum(0);
        chart.getAxisLeft().setAxisMaximum(5.0f);
        chart.getAxisRight().setDrawLabels(false);
        chart.getXAxis().setDrawLabels(false);

        vDataText = findViewById(R.id.rawVoltText);
        characteristics = new ArrayList<>();
        voltageList = new ArrayList<Float>();
        mDevice = getIntent().getParcelableExtra("bluetoothDevice");
        serviceList = new ArrayList<>();
        connectGatt();

        Handler mHandler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if(gattConnected) {
                    mGattCallback.readData(mGatt);
                }

                if (!mStopHandler) {
                    mHandler.postDelayed(this, 1000);
                }
            }
        };

// start it with:
        mHandler.post(runnable);


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
        }
        this.finish();
    }

    private void connectGatt() {
        if(mDevice != null) {
            mGattCallback = new ZeroGattCallback(true);
            mDevice.connectGatt(this,false,mGattCallback);

        }

    }



    public void connected() {
        mServicesText.setTextColor(getResources().getColor(R.color.connected,getTheme()));
        mServicesText.setVisibility(View.VISIBLE);
        gattConnected = true;

    }

    public void disconnected() {
        mServicesText.setTextColor(getResources().getColor(R.color.disconnected,getTheme()));
        mServicesText.setVisibility(View.VISIBLE);
        gattConnected = false;

    }

    public void connecting() {
        mServicesText.setTextColor(getResources().getColor(R.color.connecting,getTheme()));
        mServicesText.setVisibility(View.VISIBLE);
        gattConnected = false;

    }





    private void updateButtons(int button, boolean pressed) {
        switch (button){
            case 0:
                if(pressed) { mButtonZeroStateText.setText("True");}
                else {  mButtonZeroStateText.setText("False"); }
                return;

            case 1:
                if(pressed) { mButtonOneStateText.setText("True");}
                else {  mButtonOneStateText.setText("False"); }
                return;
        }
    }

    private void updateVData() {
        vDataText.setText(vData.toString() + "  " + updateCounter.toString());
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f,voltageList.get(0)));
        entries.add(new BarEntry(1f,voltageList.get(1)));
        entries.add(new BarEntry(2f,voltageList.get(2)));
        entries.add(new BarEntry(3f,voltageList.get(3)));
        BarDataSet set = new BarDataSet(entries, "VoltageDataSet");
        set.setValueTextSize(15.0f);
        BarData chartData = new BarData(set);
        updateCounter++;
        chart.setData(chartData);
        chart.invalidate();


    }




    public class ZeroGattCallback extends BluetoothGattCallback {

        boolean mConnected = false;

        List<BluetoothGattService> mGattServices;

        Boolean projectZeroFlag;




        public ZeroGattCallback(Boolean projectZero) {
            mGattServices = Collections.emptyList();
            projectZeroFlag = projectZero;
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_FAILURE) {
                mConnected = false;
                gatt.disconnect();
                gatt.close();
                return;
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                mConnected = false;
                gatt.disconnect();
                gatt.close();
                return;
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnected = true;
                mGatt = gatt;
                gatt.discoverServices();
                Log.e(TAG,"Connected");

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnected = false;
                gatt.disconnect();
                gatt.close();
                disconnected();
                connectGatt();
            }
        }

        boolean initRead = false;
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt,int status) {
            super.onServicesDiscovered(gatt,status);
            if(status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }
            Log.d(TAG,"Services discovered");
            buttonService = gatt.getService(BUTTON_SERVICE_UUID);
            dataService = gatt.getService(DATA_SERVICE_UUID);
            button0Char = buttonService.getCharacteristic(BUTTON_0_UUID);
            button1Char = buttonService.getCharacteristic(BUTTON_1_UUID);
            stringChar = dataService.getCharacteristic(STRING_UUID);

            characteristics.clear();
            characteristics.add(button0Char);
            characteristics.add(button1Char);
            characteristics.add(stringChar);

            gatt.setCharacteristicNotification(button0Char, true);
            gatt.setCharacteristicNotification(button1Char, true);
            gatt.setCharacteristicNotification(stringChar, true);

            Log.d(TAG,"Notifications Enabled");



            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connected();
                }
            });


        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            if(characteristic.getUuid().equals(BUTTON_0_UUID) || characteristic.getUuid().equals(BUTTON_1_UUID)) {
                int mButton;
                boolean mPressed;
                if(characteristic.getUuid().equals(BUTTON_0_UUID)) {
                    mButton = 0;
                } else {
                    mButton = 1;
                }

                if(characteristic.getValue()[0] == 1) {
                    mPressed = true;
                } else {
                    mPressed = false;
                }

                runOnUiThread(() -> updateButtons(mButton,mPressed));
            } else if( characteristic.getUuid().equals(STRING_UUID)) {
                byte[] messageBytes = characteristic.getValue();
                vData = null;
                try {
                    vData = new String(messageBytes, "UTF-8");
                    Log.d(TAG,"vData read as: " + vData);
                } catch (UnsupportedEncodingException e) {
                    vData = "0,0,0,0,0";
                    Log.e(TAG,"Data service string decoding error");
                }
                decodeVoltage(vData);
                runOnUiThread(() -> updateVData());

            }
        }

        private void decodeVoltage(String voltageData) {
            Log.d(TAG,"decodeVoltage()");
            voltageData = voltageData.replace("CV", "");
            voltageData = voltageData.replace(" ", "");
            voltageData = voltageData.replaceAll("\\s+","");
            Log.e(TAG,"Voltage Data String: " + voltageData);
            String data[] = voltageData.split(",");
            voltageList.clear();
            try {
                for (String vString: data) {
                    Log.e(TAG,"Decoding voltage: " + vString + " " + voltageList.size());
                    voltageList.add(Float.parseFloat(vString));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error decoding voltage values from string");
            }
        }

        private void subscribeToCharacteristics(BluetoothGatt gatt) {
            if(characteristics.size() == 0) return;
            BluetoothGattCharacteristic characteristic = characteristics.get(0);
            gatt.setCharacteristicNotification(characteristic, true);
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR);
            if(descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            characteristics.remove(0);
            subscribeToCharacteristics(gatt);
        }

        public void readData(BluetoothGatt gatt) {
            if(mConnected) {
                gatt.readCharacteristic(stringChar);
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG,"Characteristic Read: " + characteristic.getValue().toString());
            if(characteristic.getUuid().equals(BUTTON_0_UUID) || characteristic.getUuid().equals(BUTTON_1_UUID)) {
                int mButton;
                boolean mPressed;
                if(characteristic.getUuid().equals(BUTTON_0_UUID)) {
                    mButton = 0;
                } else {
                    mButton = 1;
                }

                if(characteristic.getValue()[0] == 1) {
                    mPressed = true;
                } else {
                    mPressed = false;
                }

                runOnUiThread(() -> updateButtons(mButton,mPressed));
            } else if( characteristic.getUuid().equals(STRING_UUID)) {
                byte[] messageBytes = characteristic.getValue();
                vData = null;
                try {
                    vData = new String(messageBytes, "UTF-8");
                    Log.d(TAG,"vData read as: " + vData);
                } catch (UnsupportedEncodingException e) {
                    vData = "0,0,0,0,0";
                    Log.e(TAG,"Data service string decoding error");
                }
                decodeVoltage(vData);
                runOnUiThread(() -> updateVData());

            }
        }





    }




}
