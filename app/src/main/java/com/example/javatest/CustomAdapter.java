package com.example.javatest;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedHashSet;

public class CustomAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflter;
    private LinkedHashSet<BluetoothDevice> mSetTest;
    private int mSelectedItem = -1;
    private boolean mConnected = false;


    public CustomAdapter(Context applicationContext) {
        this.context = applicationContext;
        inflter = (LayoutInflater.from(applicationContext));
        mSetTest = new LinkedHashSet<BluetoothDevice>();
    }

    public void clearItems() {
        mSelectedItem = -1;
        mSetTest = new LinkedHashSet<BluetoothDevice>();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mSetTest.size();
    }

    @Override
    public Object getItem(int i) {
        return mSetTest.toArray()[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void addItem(BluetoothDevice device) {
        mSetTest.add(device);
        notifyDataSetChanged();
    }

    public void selectItem(int i) {
        mSelectedItem = i;
    }

    public BluetoothDevice getSelectedDevice() {
        return (BluetoothDevice) (getItem((mSelectedItem)));
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View result;

        result = inflter.inflate(R.layout.partyhat, null);

        TextView txtName = result.findViewById(R.id.txtName);
        txtName.setText(((BluetoothDevice) (mSetTest.toArray()[i])).getName());

        TextView txtAddr = result.findViewById(R.id.txtAddress);
        txtAddr.setText(((BluetoothDevice) (mSetTest.toArray()[i])).getAddress());


        if (mConnected) {

            result.setBackgroundColor(0xFFCCEECC);
        } else if (mSelectedItem == i) {
            result.setBackgroundColor(0xFF999999);
        } else {
            result.setBackgroundColor(0xFFCCCCCC);
        }

        return result;

    }

    void setConnected(boolean c) {
        mConnected =c ;
    }

}