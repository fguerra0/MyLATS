package com.example.mylats;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeviceActivity extends AppCompatActivity
{
    private final static String TAG = DeviceActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private android.support.v7.widget.Toolbar toolbar;
    private TextView connectionState;
    private TextView dataField;
    private String deviceName;
    private String deviceAddress;
    private ExpandableListView gattServicesList;
    private BluetoothLeService bluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> gattCharacteristicsModel;
    private boolean connected = false;
    private BluetoothGattCharacteristic notify;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private final ServiceConnection serviceConnectionBluetooth = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if(!bluetoothLeService.initialize())
            {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            Log.d(TAG, "onServiceDisconnected called. Service bound, interface accessible through IBinder instance");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
        }
    };




    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))
            {
                connected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            }
            else if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action))
            {
                connected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            }
            else if(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
                displayGattServices(bluetoothLeService.getSupportedGattServices());
            }
            else if(BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))
            {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private final ExpandableListView.OnChildClickListener servicesListClickListener =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
                {
                    if(gattCharacteristicsModel != null)
                    {
                        final BluetoothGattCharacteristic characteristic =
                                gattCharacteristicsModel.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0)
                        {
                            if(notify != null)
                            {
                                bluetoothLeService.setCharacteristicNotification(notify, false);
                                notify = null;
                            }
                            bluetoothLeService.readCharacteristic(characteristic);
                        }
                        if((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)
                        {
                            notify = characteristic;
                            bluetoothLeService.setCharacteristicNotification(characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
            };

    private void clearUI()
    {
        gattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        dataField.setText(R.string.no_data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        getSupportActionBar().setTitle(deviceName);
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        ((TextView) findViewById(R.id.device_address)).setText(deviceAddress);
        gattServicesList = findViewById(R.id.gatt_services_list);
        gattServicesList.setOnChildClickListener(servicesListClickListener);
        connectionState = findViewById(R.id.connection_state);
        dataField = findViewById(R.id.data_value);
        Intent gattService = new Intent(this, BluetoothLeService.class);
        bindService(gattService, serviceConnectionBluetooth, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unbindService(serviceConnectionBluetooth);
        bluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_device, menu);
        if(connected)
        {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        }
        else
        {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.action_settings:
                return true;
            case R.id.menu_connect:
                bluetoothLeService.connect(deviceAddress);
                return true;
            case R.id.menu_disconnect:
                bluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                bluetoothLeService.close();
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data)
    {
        if(data != null)
        {
            dataField.setText(data);
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices)
    {
        if(gattServices == null) return;
        String uuid = null;
        String unknownService = getResources().getString(R.string.unknown_service);
        String unknownChar = getResources().getString(R.string.unknown_char);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData =
                new ArrayList<ArrayList<HashMap<String, String>>>();
        gattCharacteristicsModel = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        for(BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            if ((uuid.equals(GattHeartRateAttributes.UUID_HEART_RATE_SERVICE)) || (uuid.equals(GattHeartRateAttributes.UUID_BATTERY_SERVICE)))
            {
                currentServiceData.put(
                        LIST_NAME, GattHeartRateAttributes.lookup(uuid, unknownService));
                currentServiceData.put(LIST_UUID, uuid);
                gattServiceData.add(currentServiceData);
                ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                ArrayList<BluetoothGattCharacteristic> chars = new ArrayList<BluetoothGattCharacteristic>();
                for(BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
                {
                    chars.add(gattCharacteristic);
                    HashMap<String, String> currentCharData = new HashMap<String, String>();
                    uuid = gattCharacteristic.getUuid().toString();
                    currentCharData.put(
                            LIST_NAME, GattHeartRateAttributes.lookup(uuid, unknownChar));
                    currentCharData.put(LIST_UUID, uuid);
                    gattCharacteristicGroupData.add(currentCharData);
                }
                gattCharacteristicsModel.add(chars);
                gattCharacteristicData.add(gattCharacteristicGroupData);
            }
        }
        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this, gattServiceData, android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData, android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        gattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

}
