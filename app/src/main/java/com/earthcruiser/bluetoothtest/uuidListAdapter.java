package com.earthcruiser.bluetoothtest;

import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Colin on 3/1/2018.
 */

public class uuidListAdapter extends BaseAdapter {

    ArrayList<BluetoothGattService> mData;
    Context mContext;
    LayoutInflater mInflater;

    public uuidListAdapter(Context context, ArrayList<BluetoothGattService> data) {
        mContext = context;
        mData = data;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = mInflater.inflate(R.layout.list_uuid_item,viewGroup,false);

        BluetoothGattService item = mData.get(i);

        TextView uuidText = rowView.findViewById(R.id.uuidListItemText);

        uuidText.setText(item.getUuid().toString());

        return rowView;
    }
}
