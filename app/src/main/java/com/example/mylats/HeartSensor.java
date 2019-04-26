package com.example.mylats;

import android.app.Activity;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.Constants;

import java.util.ArrayList;

public class HeartSensor extends AppCompatActivity
{
    private android.support.v7.widget.Toolbar toolbar;
    private ListView listview;
    private ProgressBar progressBar;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private DeviceListAdapter deviceListAdapter;
    private Boolean scanning = false;
    private Handler handler;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;

    public ScanCallback callback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    deviceListAdapter.addDevice(result.getDevice());
                    deviceListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onScanFailed(int errorCode) {
                    Log.d("ScanCallback", "Scan failed: " + String.valueOf(errorCode));
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_sensor);
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        handler = new Handler();
        listview = findViewById(R.id.devicesListView);
        progressBar = findViewById(R.id.progress_bar);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, "BLE Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothAdapter == null)
        {
            Toast.makeText(this, "Error: Bluetooth no supported", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (!scanning)
        {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            progressBar.setVisibility(View.GONE);
            listview.setVisibility(View.VISIBLE);
        }
        else
        {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            progressBar.setVisibility(View.VISIBLE);
            listview.setVisibility(View.GONE);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_scan:
                deviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (deviceListAdapter.getCount() > 0)
        {
            outState.putParcelableArrayList("DevicesFound", deviceListAdapter.getDevicesList());
            Log.d("Checking", "Device found saved!");
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getParcelableArrayList("DevicesFound") != null)
        {
            ArrayList<BluetoothDevice> devicesList = savedInstanceState.getParcelableArrayList("DevicesFound");
            deviceListAdapter = new DeviceListAdapter(devicesList);
            listview.setAdapter(deviceListAdapter);
            Log.d("Checking", "Check State");
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d("OnResume", "Welcome back");
        final Intent intent = new Intent(this, DeviceActivity.class);
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if(deviceListAdapter == null)
        {
            deviceListAdapter = new DeviceListAdapter();
            listview.setAdapter(deviceListAdapter);
        }

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                final BluetoothDevice bluetoothDevice = deviceListAdapter.getDevice(position);
                if(bluetoothDevice == null) return;
                intent.putExtra(DeviceActivity.EXTRAS_DEVICE_NAME, bluetoothDevice.getName());
                intent.putExtra(DeviceActivity.EXTRAS_DEVICE_ADDRESS, bluetoothDevice.getAddress());
                if(scanning)
                {
                    bluetoothLeScanner.stopScan((ScanCallback) callback);
                    scanning = false;
                }
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED)
            finish();
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable)
    {
        if(enable)
        {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    bluetoothLeScanner.stopScan(callback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            scanning = true;
            bluetoothLeScanner.startScan(callback);
        }
        else
        {
            scanning = false;
            bluetoothLeScanner.stopScan(callback);
        }
        invalidateOptionsMenu();
    }

    private class DeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> devicesList;
        private LayoutInflater inflater;

        public DeviceListAdapter() {
            super();
            devicesList = new ArrayList<BluetoothDevice>();
            inflater = HeartSensor.this.getLayoutInflater();
        }

        public DeviceListAdapter(ArrayList<BluetoothDevice> list) {
            super();
            devicesList = new ArrayList<BluetoothDevice>(list);
            inflater = HeartSensor.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!devicesList.contains(device))
                devicesList.add(device);
        }

        public ArrayList getDevicesList() {
            return devicesList;
        }

        public BluetoothDevice getDevice(int position) {
            return devicesList.get(position);
        }

        public void clear() {
            devicesList.clear();
        }

        @Override
        public int getCount() {
            return devicesList.size();
        }

        @Override
        public Object getItem(int i) {
            return devicesList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = inflater.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = view.findViewById(R.id.device_address);
                viewHolder.deviceName = view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = devicesList.get(i);
            final String deviceName = device.getName();
            final String deviceAddress = device.getAddress();
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.deviceName.setText(deviceName);
                viewHolder.deviceAddress.setText(deviceAddress);
            } else {
                viewHolder.deviceName.setText(R.string.unknown_device);
                viewHolder.deviceAddress.setText(device.getAddress());
            }
            return view;
        }
    }

    static class ViewHolder
    {
        TextView deviceName;
        TextView deviceAddress;
    }
}

