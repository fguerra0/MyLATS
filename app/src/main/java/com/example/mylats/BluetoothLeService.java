package com.example.mylats;

import android.app.Service;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.mylats.GattHeartRateAttributes;
import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service
{
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private String bluetoothDeviceAddress;
    private BluetoothGatt bluetoothGatt;
    private int connectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.example.heartsensortest.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.heartsensortest.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.heartsensortest.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.heartsensortest.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.heartsensortest.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE = UUID.fromString(GattHeartRateAttributes.UUID_HEART_RATE);

    private final IBinder iBinder = new LocalBinder();

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if(newState == BluetoothProfile.STATE_CONNECTED)
            {
                connectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                Log.d(TAG, "onConnectionStateChange. Connected to GATT server.");
                Log.d(TAG, "Attempting to start service discovery:"+ bluetoothGatt.discoverServices());

            }
            else if(newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                connectionState = STATE_DISCONNECTED;
                Log.d(TAG, "Disconnected from GATT server.");
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS)
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            else
                Log.w(TAG, "onServicesDiscovered received: "+status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS)
            {
                Log.d(TAG, "onCharacteristicRead() called");
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged() called. Heart rate changed.");
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.d(TAG, "onBind() called");
        return iBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.d(TAG, "Releasing resources, onUnbind() called");
        close();
        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder
    {
        public BluetoothLeService getService()
        {
            return BluetoothLeService.this;
        }
    }

    private void broadcastUpdate(final String action)
    {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic)
    {
        final Intent intent = new Intent(action);
        if(UUID_HEART_RATE.equals(characteristic.getUuid()))
        {
            int flag = characteristic.getProperties();
            int format = -1;
            if((flag & 0x01) != 0)
            {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            }
            else
            {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        }
        else
        {
            final byte[] data = characteristic.getValue();
            if(data != null && data.length > 0)
            {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    public boolean initialize()
    {
        if(bluetoothManager == null)
        {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if(bluetoothManager == null)
            {
                Log.e(TAG, "Unable to initialize Bluetooth.");
                return false;
            }
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        if(bluetoothAdapter == null)
        {
            Log.e(TAG, "Unable to obtain a Bluetooth adapter.");
            return false;
        }
        return true;
    }

    public boolean connect(final String address)
    {
        Log.d(TAG, "GATT client-server connection. Starting connection...");
        if(bluetoothAdapter == null || address == null)
        {
            Log.w(TAG, "Bluetooth adapter not initialized or unspecified address.");
            return false;
        }

        if(bluetoothDeviceAddress != null && address.equals(bluetoothDeviceAddress) && bluetoothGatt != null)
        {
            Log.d(TAG, "Trying to use an existing connection.");
            if(bluetoothGatt.connect())
            {
                connectionState = STATE_CONNECTING;
                return true;
            }
            else
                return false;
        }
        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if(device == null)
        {
            Log.w(TAG, "Device not found.");
            return false;
        }
        Log.d(TAG, "Device found, connection ongoing...");
        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        bluetoothDeviceAddress = address;
        connectionState = STATE_CONNECTING;
        return true;
    }

    public void disconnect()
    {
        if(bluetoothAdapter == null || bluetoothGatt == null)
        {
            Log.w(TAG, "Bluetooth adapter not initialized.");
            return;
        }
        bluetoothGatt.disconnect();
    }

    public void close()
    {
        Log.d(TAG, "close() called.");
        if(bluetoothGatt == null)
            return;
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if(bluetoothAdapter == null || bluetoothGatt == null)
        {
            Log.w(TAG, "Bluetooth adapter not initialized.");
            return;
        }
        bluetoothGatt.readCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled)
    {
        if(bluetoothAdapter == null || bluetoothGatt == null)
        {
            Log.w(TAG, "Bluetooth adapter not initialized.");
            return;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        if(UUID_HEART_RATE.equals(characteristic.getUuid()))
        {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(GattHeartRateAttributes.UUID_CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
    }

    public List<BluetoothGattService> getSupportedGattServices()
    {
        if(bluetoothGatt == null)
            return null;
        return bluetoothGatt.getServices();
    }
}
