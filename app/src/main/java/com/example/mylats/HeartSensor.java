package com.example.mylats;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class HeartSensor extends AppCompatActivity
{
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 30000;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private final static String TAG = HeartSensor.class.getSimpleName();
    private BluetoothDevice mDevice;

    private final static int UPDATE_DEVICE = 0;
    private final static int UPDATE_VALUE = 1;
    private final Handler uiHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            final int what = msg.what;
            final String value = (String) msg.obj;
            switch(what)
            {
                case UPDATE_DEVICE:
                    updateDevice(value);
                    break;
                case UPDATE_VALUE:
                    updateValue(value);
                    break;
            }
        }
    };

    private void updateDevice(String devName)
    {
        TextView t = findViewById(R.id.dev_type);
        t.setText(devName);
    }

    private void updateValue(String value)
    {
        TextView t = findViewById(R.id.value_read);
        t.setText(value);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_sensor);
        mHandler = new Handler();
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, "BLE Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        switch(requestCode)
        {
            case PERMISSION_REQUEST_COARSE_LOCATION:
            {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    scanLeDevice(true);
                else
                    scanLeDevice(false);
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else
        {
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();

            filters = new ArrayList<ScanFilter>();
        }
        scanLeDevice(true);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if(mBluetoothAdapter != null && mBluetoothAdapter.isEnabled())
        {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onDestroy()
    {
        if(mGatt == null)
            return;
        mGatt.close();
        mGatt = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == REQUEST_ENABLE_BT)
        {
            if(resultCode == Activity.RESULT_CANCELED)
            {
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable)
    {
        if(enable)
        {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run()
                {
                    mLEScanner.stopScan(mScanCallback);
                }
            }, SCAN_PERIOD);
            mLEScanner.startScan(filters, settings, mScanCallback);
        }
        else
        {
            mLEScanner.stopScan(mScanCallback);
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            Log.i("callbackType", String.valueOf(callbackType));
            String devicename = result.getDevice().getName();
            if(devicename != null) {
                Log.i("result", "Device name:" + devicename);
                Log.i("result", result.toString());
                BluetoothDevice btDevice = result.getDevice();
                connectToDevice(btDevice);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results)
        {
            for(ScanResult sr : results)
                Log.i("ScanResult - Results", sr.toString());
        }

        @Override
        public void onScanFailed(int errorCode)
        {
            Log.e("Scan Failed", "Error Code: "+errorCode);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {
                            Log.i("onLeScan", device.toString());
                            connectToDevice(device);
                        }
                    });
                }
            };

    public void connectToDevice(BluetoothDevice device)
    {
        if(mGatt == null)
        {
            Log.d("connectToDevice", "connecting to device: "+device.toString());
            this.mDevice = device;
            mGatt = device.connectGatt(this, false, gattCallback);
            scanLeDevice(false);
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            Log.i("onConnectionStateChange", "Status: "+status);
            switch(newState)
            {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    Message msg = Message.obtain();
                    msg.obj = "Heart Rate";
                    msg.what = 0;
                    msg.setTarget(uiHandler);
                    msg.sendToTarget();
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    Log.i("gattCallback", "Reconnecting...");
                    BluetoothDevice mDevice = gatt.getDevice();
                    mGatt = null;
                    connectToDevice(mDevice);
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            mGatt = gatt;
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
            BluetoothGattCharacteristic hr_char = services.get(2).getCharacteristics().get(0);

            for(BluetoothGattDescriptor descriptor : hr_char.getDescriptors())
            {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                mGatt.writeDescriptor(descriptor);
            }
            gatt.setCharacteristicNotification(hr_char, true);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            Log.i("onCharacteristicRead", characteristic.toString());
            byte[] value = characteristic.getValue();
            String v = new String(value);
            Log.i("onCharacteristicRead", "Value: "+v);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            float char_float_value = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 1);
            Log.i("onCharacteristicChanged", Float.toString(char_float_value));
            String value = Float.toString(char_float_value);
            Message msg = Message.obtain();
            msg.obj = value;
            msg.what = 1;
            msg.setTarget(uiHandler);
            msg.sendToTarget();
        }
    };
}


