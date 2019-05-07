package com.example.mylats;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ArduinoScreen extends Activity
{
    Button btnUp, btnDown, btnReturn;
    TextView txtArduino, txtString, txtStringLength, sensorView;
    Handler btIn;

    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
   // private ConnectedThread mConnectedThread;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address;

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arduino);
        btnUp = findViewById(R.id.button_up);
        btnDown = findViewById(R.id.button_down);
        btnReturn = findViewById(R.id.go_back);
        sensorView = findViewById(R.id.sensorView0);
        btIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                String read = (String) msg.obj;
                recDataString.append(read);
                int endOfLineIndex = recDataString.indexOf("~");
                if (endOfLineIndex > 0) {
                    String dataInPrint = recDataString.substring(0, endOfLineIndex);
                    txtString.setText("Data received: " + dataInPrint);
                    if (recDataString.charAt(0) == '#') {
                        String sensor0 = recDataString.substring(1, 5);
                        sensorView.setText(" Arduino Voltage = " + sensor0 + "V");
                    }
                    recDataString.delete(0 ,recDataString.length());
                    dataInPrint = " ";
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
       // checkBTState();

        btnDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //mConnectedThread.write("0");
                Toast.makeText(getBaseContext(), "Turn down voltage", Toast.LENGTH_SHORT).show();
            }
        });

        btnUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //mConnectedThread.write("1");
                Toast.makeText(getBaseContext(), "Turn up voltage", Toast.LENGTH_SHORT).show();
            }
        });

        btnReturn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });
    }
/*
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Intent intent = getIntent();
        address = intent.getStringExtra(DeviceActivity.EXTRAS_DEVICE_ADDRESS);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        try
        {
            btSocket = createBluetoothSocket(device);
        }
        catch(IOException e)
        {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        try
        {
            btSocket.connect();
        }
        catch(IOException e)
        {
            try
            {
                btSocket.close();
            }
            catch (IOException e2)
            {
                e2.printStackTrace();
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
        mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            btSocket.close();
        }
        catch(IOException e2)
        {
            e2.printStackTrace();
        }
    }

    private void checkBTState()
    {
        if(btAdapter == null)
        {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        }
        else
        {
            if(!btAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            while(true)
            {
                try
                {
                    bytes = mmInStream.read(buffer);
                    String readMsg = new String(buffer, 0, bytes);
                    btIn.obtainMessage(handlerState, bytes, -1, readMsg).sendToTarget();
                }
                catch(IOException e)
                {
                    break;
                }
            }
        }

        public void write(String input)
        {
            byte[] msgBuffer = input.getBytes();
            try
            {
                mmOutStream.write(msgBuffer);
            }
            catch(IOException e)
            {
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }*/

}
