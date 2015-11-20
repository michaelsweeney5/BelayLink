package com.example.msweeney.btbelaylink;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final int SUCCESS_CONNECT = 0;
    public static final int MESSAGE_READ = 1;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    ArrayAdapter<String> listAdapter;
    ArrayList<String> pairedDevices;
    ArrayList<BluetoothDevice> bluetoothDevices;
    Set<BluetoothDevice> devicesSet;
    ConnectThread connectThread;
    ConnectedThread connectedThread;
    ListView listView;
    Button offBelayButton;
    Button thatsMeButton;
    Button haulingRopeButton;
    Button onBelayButton;
    Button climbingButton;
    BluetoothAdapter btAdapter;
    IntentFilter filter;
    BroadcastReceiver receiver;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what) {
                case SUCCESS_CONNECT:
                    //do something
                    ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
                    Toast.makeText(getApplicationContext(), "CONNECTED",Toast.LENGTH_LONG).show();
                    connectedThread.start();
                    break;
                case MESSAGE_READ:
                    //do something
                    byte[] readBuf = (byte[])msg.obj;
                    String string = new String(readBuf);
                    Toast.makeText(getApplicationContext(), string,Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        if(btAdapter == null) {
            Toast.makeText(getApplicationContext(),"Must have bluetooth to use this app.",Toast.LENGTH_SHORT). show();
            finish();
        }
        else {
            if(!btAdapter.isEnabled()) {
                turnOnBluetooth();
            }
            getPairedDevices();
            btAdapter.startDiscovery();
        }
    }

    public void startDiscovery() {
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }

    public void turnOnBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, 1);
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }

    public void getPairedDevices() {
        devicesSet = btAdapter.getBondedDevices();
        if(!devicesSet.isEmpty()) {
            for(BluetoothDevice device : devicesSet) {
                bluetoothDevices.add(device);
                listAdapter.add(device.getName() + device.getAddress());
            }
        }
    }

    public void init() {
        listView = (ListView)findViewById(R.id.devicesListView);
        listView.setOnItemClickListener(this);
        offBelayButton = (Button)findViewById(R.id.offBelayButton);
        haulingRopeButton = (Button)findViewById(R.id.haulingRopeButton);
        thatsMeButton = (Button)findViewById(R.id.thatsMeButton);
        onBelayButton = (Button)findViewById(R.id.onBelayButton);
        climbingButton = (Button)findViewById(R.id.climbingButton);
        offBelayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                offBelayClicked(connectedThread);
            }
        });
        haulingRopeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                haulingRopeClicked(connectedThread);
            }
        });
        thatsMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thatsMeClicked(connectedThread);
            }
        });
        onBelayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBelayClicked(connectedThread);
            }
        });
        climbingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                climbingClicked(connectedThread);
            }
        });
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_selectable_list_item,0);
        listView.setAdapter(listAdapter);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = new ArrayList<String>();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        bluetoothDevices = new ArrayList<BluetoothDevice>();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    bluetoothDevices.add(device);
                    listAdapter.add(device.getName() + device.getAddress());
                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    //code
                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                }
                else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    if(btAdapter.getState()==btAdapter.STATE_OFF) {
                        turnOnBluetooth();
                    }
                }

            }
        };
        registerReceiver(receiver,filter);
         filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver,filter);
         filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver,filter);
         filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(),"Must enable bluetooth to use this app.",Toast.LENGTH_SHORT). show();
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }
        BluetoothDevice selectedDevice = bluetoothDevices.get(position);
        Toast.makeText(getApplicationContext(),selectedDevice.getAddress(),Toast.LENGTH_SHORT). show();
        connectThread = new ConnectThread(selectedDevice);
        connectThread.start();
    }

    public class ConnectThread extends Thread {
        public final BluetoothSocket mmSocket;
        public final BluetoothDevice mmDevice;
        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            btAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
            mHandler.obtainMessage(SUCCESS_CONNECT,mmSocket).sendToTarget();
        }

        public void manageConnectedSocket(BluetoothSocket mmSocket) {
            connectedThread = new ConnectedThread(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    public class ConnectedThread extends Thread {
        public final BluetoothSocket mmSocket;
        public final InputStream mmInStream;
        public final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer;  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    buffer = new byte[1024];
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();

                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    public void offBelayClicked(ConnectedThread connectedThread) {
        if(connectThread != null) {
            if (connectThread.mmSocket.isConnected()) {
                byte[] buf;
                buf = "1".getBytes(Charset.forName("UTF-8"));
                connectedThread.write(buf);
            } else {
                Toast.makeText(getApplicationContext(), "You must first connect bluetooth.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "You must first connect bluetooth.", Toast.LENGTH_SHORT).show();
        }

    }
    public void haulingRopeClicked(ConnectedThread connectedThread) {
        if(connectThread != null) {
            if (connectThread.mmSocket.isConnected()) {
                byte[] buf;
                buf = "2".getBytes(Charset.forName("UTF-8"));
                connectedThread.write(buf);
            } else {
                Toast.makeText(getApplicationContext(), "You must first connect bluetooth.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "You must first connect bluetooth.", Toast.LENGTH_SHORT).show();
        }
    }
    public void thatsMeClicked(ConnectedThread connectedThread) {
        if(connectThread != null) {
            if (connectThread.mmSocket.isConnected()) {
                byte[] buf;
                buf = "3".getBytes(Charset.forName("UTF-8"));
                connectedThread.write(buf);
            } else {
                Toast.makeText(getApplicationContext(), "You must first connect bluetooth.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "You must first connect bluetooth.", Toast.LENGTH_SHORT).show();
        }
    }
    public void onBelayClicked(ConnectedThread connectedThread) {
        if(connectThread != null) {
            if (connectThread.mmSocket.isConnected()) {
                byte[] buf;
                buf = "4".getBytes(Charset.forName("UTF-8"));
                connectedThread.write(buf);
            } else {
                Toast.makeText(getApplicationContext(), "You must first connect bluetooth.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "You must first connect bluetooth.", Toast.LENGTH_SHORT).show();
        }
    }
    public void climbingClicked(ConnectedThread connectedThread) {
        if(connectThread != null) {
            if (connectThread.mmSocket.isConnected()) {
                byte[] buf;
                buf = "5".getBytes(Charset.forName("UTF-8"));
                connectedThread.write(buf);
            } else {
                Toast.makeText(getApplicationContext(), "You must first connect bluetooth.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "You must first connect bluetooth.", Toast.LENGTH_SHORT).show();
        }
    }
}