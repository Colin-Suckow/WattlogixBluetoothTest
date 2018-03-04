package com.earthcruiser.bluetoothtest;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Colin on 2/25/2018.
 */

public class GenericGattCallback extends BluetoothGattCallback {

    boolean mConnected = false;

    List<BluetoothGattService> mGattServices;

    Boolean projectZeroFlag;




    public GenericGattCallback(Boolean projectZero) {
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
            gatt.discoverServices();

        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mConnected = false;
            gatt.disconnect();
            gatt.close();
        }
    }
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt,int status) {
        super.onServicesDiscovered(gatt,status);
        if(status != BluetoothGatt.GATT_SUCCESS) {
            return;
        }
        mGattServices = gatt.getServices();
    }

    public List<BluetoothGattService> getGattServices() {
        if(mGattServices.size() != 0) {
            Log.d(TAG,"Got Gatt Services");

            for (BluetoothGattService service:mGattServices) {
                Log.d(TAG, "Found Gatt Service: " + service.getUuid().toString());
            }
            return mGattServices;
        }

        Log.e(TAG,"Empty Gatt Services List");
        return Collections.emptyList();
    }

}

