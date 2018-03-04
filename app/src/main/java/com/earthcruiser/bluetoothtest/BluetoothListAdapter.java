package com.earthcruiser.bluetoothtest;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Colin on 2/19/2018.
 */

public class BluetoothListAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<BluetoothDevice> mDataSource;
    private  BluetoothDevice device;


    public BluetoothListAdapter(Context context, ArrayList<BluetoothDevice> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int i) {
        return mDataSource.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public BluetoothDevice getDevice() {
        return device;
    }



    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = mInflater.inflate(R.layout.list_device_item, viewGroup,false);

        TextView nameText = (TextView) rowView.findViewById(R.id.nameText);
        TextView idText = (TextView) rowView.findViewById(R.id.idText);

        device = (BluetoothDevice) getItem(i);



        if(device.getName() == null) {
            nameText.setText("Null");
        } else {
            nameText.setText(device.getName());
        }
        idText.setText(device.getAddress());

        return rowView;
    }
}
