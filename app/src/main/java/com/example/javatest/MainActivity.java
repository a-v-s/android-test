package com.example.javatest;


import static android.bluetooth.BluetoothProfile.GATT;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


// Resources:
//
// * https://punchthrough.com/android-ble-guide/

public class MainActivity extends AppCompatActivity {
    Timer timer;
    TimerTask timerTask;
    BluetoothLeScanner btScanner;


    Button btnPermission;
    Button btnScan;
    Button btnConnect;
    Button btnDisconnect;

    Button btnRbDim;
    Button btnRbMid;
    Button btnRbBright;

    Button btnCfDim;
    Button btnCfMid;
    Button btnCfBright;

    Button btnCvDim;
    Button btnCvMid;
    Button btnCvBright;

    Button btnWalkDim;
    Button btnWalkMid;
    Button btnWalkBright;

    Button btnSpdStop;
    Button btnSpdSlow;
    Button btnSpdMid;
    Button btnSpdFast;

    Button btnDirRight;
    Button btnDirLeft;

    ListView lvHats;


    private BluetoothDevice m_device;
    private BluetoothGatt m_gatt;

    private CustomAdapter mCustomAdapter;
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            ScanRecord record = result.getScanRecord();
            if (record != null) {
                List<ParcelUuid> services = record.getServiceUuids();
                if (services != null) {

                    ParcelUuid hatUUID = ParcelUuid.fromString("65dbc53e-0000-4422-947c-f016c0e0af10");
                    if (services.contains(hatUUID)) {
                        mCustomAdapter.addItem(result.getDevice());
                        mCustomAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("hat", "onCreate called");


        // Whatever the example does
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        btnPermission = findViewById(R.id.btnPermission);
        btnScan = findViewById(R.id.btnScan);
        btnConnect = findViewById(R.id.btnConnect);
        btnDisconnect = findViewById(R.id.btnDisconnect);

        btnRbDim = findViewById(R.id.btnRbDim);
        btnRbMid = findViewById(R.id.btnRbMid);
        btnRbBright = findViewById(R.id.btnRbBright);

        btnCfDim = findViewById(R.id.btnCfDim);
        btnCfMid = findViewById(R.id.btnCfMid);
        btnCfBright = findViewById(R.id.btnCfBright);

        btnCvDim = findViewById(R.id.btnCvDim);
        btnCvMid = findViewById(R.id.btnCvMid);
        btnCvBright = findViewById(R.id.btnCvBright);

         btnWalkDim = findViewById(R.id.btnWalkDim);;
         btnWalkMid = findViewById(R.id.btnWalkMid);;
         btnWalkBright = findViewById(R.id.btnWalkBright);;

        btnSpdStop = findViewById(R.id.btnSpdStop);
        btnSpdSlow = findViewById(R.id.btnSpdSlow);
        btnSpdMid = findViewById(R.id.btnSpdMid);
        btnSpdFast = findViewById(R.id.btnSpdFast);

        btnDirRight = findViewById(R.id.btnDirRight);
        btnDirLeft = findViewById(R.id.btnDirLeft);

        btnPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askForPermission();
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                scanForDevices();
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {

            // Permissions, please? We have bluetooth permission else we wouldn't be connected
            // in the first place, would we?
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (null != m_gatt)
                    m_gatt.disconnect();
            }
        });


        btnConnect.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                m_device = mCustomAdapter.getSelectedDevice();
                connect();


            }
        });


        btnRbBright.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                byte[] data = new byte[]{
                        (byte) 0xFF, (byte) 0x00, (byte) 0x00,
                        (byte) 0xFF, (byte) 0x55, (byte) 0x00,
                        (byte) 0xFF, (byte) 0xAA, (byte) 0x00,
                        (byte) 0xFF, (byte) 0xFF, (byte) 0x00,
                        (byte) 0xA9, (byte) 0xFF, (byte) 0x00,
                        (byte) 0x54, (byte) 0xFF, (byte) 0x00,
                        (byte) 0x00, (byte) 0xFF, (byte) 0x00,
                        (byte) 0x00, (byte) 0xFF, (byte) 0x55,
                        (byte) 0x00, (byte) 0xFF, (byte) 0xAA,
                        (byte) 0x00, (byte) 0xFF, (byte) 0xFF,
                        (byte) 0x00, (byte) 0xA9, (byte) 0xFF,
                        (byte) 0x00, (byte) 0x55, (byte) 0xFF,
                        (byte) 0x00, (byte) 0x00, (byte) 0xFF,
                        (byte) 0x54, (byte) 0x00, (byte) 0xFF,
                        (byte) 0xAA, (byte) 0x00, (byte) 0xFF,
                        (byte) 0xFF, (byte) 0x00, (byte) 0xFF,
                        (byte) 0xFF, (byte) 0x00, (byte) 0xA9,
                        (byte) 0xFF, (byte) 0x00, (byte) 0x55,
                };
                setLeds(data);
            }
        });

        btnRbMid.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {

                byte[] data = new byte[]{
                        (byte) 0x3F, (byte) 0x00, (byte) 0x00,
                        (byte) 0x3F, (byte) 0x15, (byte) 0x00,
                        (byte) 0x3F, (byte) 0x2A, (byte) 0x00,
                        (byte) 0x3F, (byte) 0x3F, (byte) 0x00,
                        (byte) 0x2A, (byte) 0x3F, (byte) 0x00,
                        (byte) 0x15, (byte) 0x3F, (byte) 0x00,
                        (byte) 0x00, (byte) 0x3F, (byte) 0x00,
                        (byte) 0x00, (byte) 0x3F, (byte) 0x15,
                        (byte) 0x00, (byte) 0x3F, (byte) 0x2A,
                        (byte) 0x00, (byte) 0x3F, (byte) 0x3F,
                        (byte) 0x00, (byte) 0x2A, (byte) 0x3F,
                        (byte) 0x00, (byte) 0x15, (byte) 0x3F,
                        (byte) 0x00, (byte) 0x00, (byte) 0x3F,
                        (byte) 0x15, (byte) 0x00, (byte) 0x3F,
                        (byte) 0x2A, (byte) 0x00, (byte) 0x3F,
                        (byte) 0x3F, (byte) 0x00, (byte) 0x3F,
                        (byte) 0x3F, (byte) 0x00, (byte) 0x2A,
                        (byte) 0x3F, (byte) 0x00, (byte) 0x15,
                };
                setLeds(data);
            }
        });


        btnRbDim.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {

                byte[] data = new byte[]{
                        (byte) 0x0C, (byte) 0x00, (byte) 0x00,
                        (byte) 0x0C, (byte) 0x04, (byte) 0x00,
                        (byte) 0x0C, (byte) 0x08, (byte) 0x00,
                        (byte) 0x0C, (byte) 0x0C, (byte) 0x00,
                        (byte) 0x08, (byte) 0x0C, (byte) 0x00,
                        (byte) 0x04, (byte) 0x0C, (byte) 0x00,
                        (byte) 0x00, (byte) 0x0C, (byte) 0x00,
                        (byte) 0x00, (byte) 0x0C, (byte) 0x04,
                        (byte) 0x00, (byte) 0x0C, (byte) 0x08,
                        (byte) 0x00, (byte) 0x0C, (byte) 0x0C,
                        (byte) 0x00, (byte) 0x08, (byte) 0x0C,
                        (byte) 0x00, (byte) 0x04, (byte) 0x0C,
                        (byte) 0x00, (byte) 0x00, (byte) 0x0C,
                        (byte) 0x04, (byte) 0x00, (byte) 0x0C,
                        (byte) 0x08, (byte) 0x00, (byte) 0x0C,
                        (byte) 0x0C, (byte) 0x00, (byte) 0x0C,
                        (byte) 0x0C, (byte) 0x00, (byte) 0x08,
                        (byte) 0x0C, (byte) 0x00, (byte) 0x04,

                };
                setLeds(data);
            }
        });


        btnCfBright.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                byte[] data = new byte[]{
                        (byte) 0x5F, (byte) 0x5F, (byte) 0x5F,
                        (byte) 0x4F, (byte) 0x64, (byte) 0x64,
                        (byte) 0x3F, (byte) 0x69, (byte) 0x69,
                        (byte) 0x2F, (byte) 0x6F, (byte) 0x6F,
                        (byte) 0x1F, (byte) 0x74, (byte) 0x74,
                        (byte) 0x0F, (byte) 0x79, (byte) 0x79,
                        (byte) 0x00, (byte) 0x7F, (byte) 0x7F,
                        (byte) 0x15, (byte) 0x7F, (byte) 0x69,
                        (byte) 0x2A, (byte) 0x7F, (byte) 0x54,
                        (byte) 0x3F, (byte) 0x7F, (byte) 0x3F,
                        (byte) 0x54, (byte) 0x7F, (byte) 0x2A,
                        (byte) 0x69, (byte) 0x7F, (byte) 0x15,
                        (byte) 0x7F, (byte) 0x7F, (byte) 0x00,
                        (byte) 0x79, (byte) 0x79, (byte) 0x0F,
                        (byte) 0x74, (byte) 0x74, (byte) 0x1F,
                        (byte) 0x6F, (byte) 0x6F, (byte) 0x2F,
                        (byte) 0x69, (byte) 0x69, (byte) 0x3F,
                        (byte) 0x64, (byte) 0x64, (byte) 0x4F,
                };
                setLeds(data);

            }
        });

        btnCfMid.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                byte[] data = new byte[]{
                        (byte) 0x17, (byte) 0x17, (byte) 0x17,
                        (byte) 0x13, (byte) 0x19, (byte) 0x19,
                        (byte) 0x0F, (byte) 0x1A, (byte) 0x1A,
                        (byte) 0x0B, (byte) 0x1B, (byte) 0x1B,
                        (byte) 0x07, (byte) 0x1D, (byte) 0x1D,
                        (byte) 0x03, (byte) 0x1E, (byte) 0x1E,
                        (byte) 0x00, (byte) 0x1F, (byte) 0x1F,
                        (byte) 0x05, (byte) 0x1F, (byte) 0x1A,
                        (byte) 0x0A, (byte) 0x1F, (byte) 0x15,
                        (byte) 0x0F, (byte) 0x1F, (byte) 0x0F,
                        (byte) 0x15, (byte) 0x1F, (byte) 0x0A,
                        (byte) 0x1A, (byte) 0x1F, (byte) 0x05,
                        (byte) 0x1F, (byte) 0x1F, (byte) 0x00,
                        (byte) 0x1E, (byte) 0x1E, (byte) 0x03,
                        (byte) 0x1D, (byte) 0x1D, (byte) 0x07,
                        (byte) 0x1B, (byte) 0x1B, (byte) 0x0B,
                        (byte) 0x1A, (byte) 0x1A, (byte) 0x0F,
                        (byte) 0x19, (byte) 0x19, (byte) 0x13,
                };
                setLeds(data);
            }
        });


        btnCfDim.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                byte[] data = new byte[]{
                        (byte) 0x04, (byte) 0x04, (byte) 0x04,
                        (byte) 0x03, (byte) 0x05, (byte) 0x05,
                        (byte) 0x03, (byte) 0x05, (byte) 0x05,
                        (byte) 0x02, (byte) 0x05, (byte) 0x05,
                        (byte) 0x01, (byte) 0x05, (byte) 0x05,
                        (byte) 0x00, (byte) 0x06, (byte) 0x06,
                        (byte) 0x00, (byte) 0x06, (byte) 0x06,
                        (byte) 0x01, (byte) 0x06, (byte) 0x05,
                        (byte) 0x02, (byte) 0x06, (byte) 0x04,
                        (byte) 0x03, (byte) 0x06, (byte) 0x03,
                        (byte) 0x04, (byte) 0x06, (byte) 0x02,
                        (byte) 0x05, (byte) 0x06, (byte) 0x01,
                        (byte) 0x06, (byte) 0x06, (byte) 0x00,
                        (byte) 0x06, (byte) 0x06, (byte) 0x00,
                        (byte) 0x05, (byte) 0x05, (byte) 0x01,
                        (byte) 0x05, (byte) 0x05, (byte) 0x02,
                        (byte) 0x05, (byte) 0x05, (byte) 0x03,
                        (byte) 0x05, (byte) 0x05, (byte) 0x03,
                };
                setLeds(data);
            }

        });


        btnCvBright.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (m_gatt != null) {
                    byte[] data = new byte[]{
                            (byte) 0x00, (byte) 0xFF, (byte) 0x00,
                            (byte) 0x15, (byte) 0xE9, (byte) 0x00,
                            (byte) 0x2A, (byte) 0xD4, (byte) 0x00,
                            (byte) 0x3F, (byte) 0xBF, (byte) 0x00,
                            (byte) 0x54, (byte) 0xA9, (byte) 0x00,
                            (byte) 0x69, (byte) 0x94, (byte) 0x00,
                            (byte) 0x7F, (byte) 0x7F, (byte) 0x00,
                            (byte) 0x7F, (byte) 0x69, (byte) 0x00,
                            (byte) 0x7F, (byte) 0x54, (byte) 0x00,
                            (byte) 0x7F, (byte) 0x3F, (byte) 0x00,
                            (byte) 0x7F, (byte) 0x2A, (byte) 0x00,
                            (byte) 0x7F, (byte) 0x15, (byte) 0x00,
                            (byte) 0x7F, (byte) 0x00, (byte) 0x00,
                            (byte) 0x69, (byte) 0x2A, (byte) 0x00,
                            (byte) 0x54, (byte) 0x54, (byte) 0x00,
                            (byte) 0x3F, (byte) 0x7F, (byte) 0x00,
                            (byte) 0x2A, (byte) 0xA9, (byte) 0x00,
                            (byte) 0x15, (byte) 0xD4, (byte) 0x00,
                    };
                    setLeds(data);
                }
            }
        });


        btnCvMid.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                byte[] data = new byte[]{
                        (byte) 0x00, (byte) 0x3F, (byte) 0x00,
                        (byte) 0x05, (byte) 0x3A, (byte) 0x00,
                        (byte) 0x0A, (byte) 0x35, (byte) 0x00,
                        (byte) 0x0F, (byte) 0x2F, (byte) 0x00,
                        (byte) 0x15, (byte) 0x2A, (byte) 0x00,
                        (byte) 0x1A, (byte) 0x25, (byte) 0x00,
                        (byte) 0x1F, (byte) 0x1F, (byte) 0x00,
                        (byte) 0x1F, (byte) 0x1A, (byte) 0x00,
                        (byte) 0x1F, (byte) 0x15, (byte) 0x00,
                        (byte) 0x1F, (byte) 0x0F, (byte) 0x00,
                        (byte) 0x1F, (byte) 0x0A, (byte) 0x00,
                        (byte) 0x1F, (byte) 0x05, (byte) 0x00,
                        (byte) 0x1F, (byte) 0x00, (byte) 0x00,
                        (byte) 0x1A, (byte) 0x0A, (byte) 0x00,
                        (byte) 0x15, (byte) 0x15, (byte) 0x00,
                        (byte) 0x0F, (byte) 0x1F, (byte) 0x00,
                        (byte) 0x0A, (byte) 0x2A, (byte) 0x00,
                        (byte) 0x05, (byte) 0x35, (byte) 0x00,
                };
                setLeds(data);
            }
        });


        btnCvDim.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                byte[] data = new byte[]{
                        (byte) 0x00, (byte) 0x0C, (byte) 0x00,
                        (byte) 0x01, (byte) 0x0B, (byte) 0x00,
                        (byte) 0x02, (byte) 0x0A, (byte) 0x00,
                        (byte) 0x03, (byte) 0x09, (byte) 0x00,
                        (byte) 0x04, (byte) 0x08, (byte) 0x00,
                        (byte) 0x05, (byte) 0x07, (byte) 0x00,
                        (byte) 0x06, (byte) 0x06, (byte) 0x00,
                        (byte) 0x06, (byte) 0x05, (byte) 0x00,
                        (byte) 0x06, (byte) 0x04, (byte) 0x00,
                        (byte) 0x06, (byte) 0x03, (byte) 0x00,
                        (byte) 0x06, (byte) 0x02, (byte) 0x00,
                        (byte) 0x06, (byte) 0x01, (byte) 0x00,
                        (byte) 0x06, (byte) 0x00, (byte) 0x00,
                        (byte) 0x05, (byte) 0x02, (byte) 0x00,
                        (byte) 0x04, (byte) 0x04, (byte) 0x00,
                        (byte) 0x03, (byte) 0x06, (byte) 0x00,
                        (byte) 0x02, (byte) 0x08, (byte) 0x00,
                        (byte) 0x01, (byte) 0x0A, (byte) 0x00,
                };
                setLeds(data);

            }
        });



        btnWalkBright.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                byte[] data = new byte[]{
                        (byte) 0x7f, (byte) 0x7f, (byte) 0x7f,
                        (byte) 0x7f, (byte) 0x7f, (byte) 0x7f,
                        (byte) 0x7f, (byte) 0x7f, (byte) 0x7f,
                        (byte) 0x7f, (byte) 0x7f, (byte) 0x7f,
                        (byte) 0x7f, (byte) 0x7f, (byte) 0x7f,
                        (byte) 0x7f, (byte) 0x7f, (byte) 0x7f,
                        (byte) 0x7f, (byte) 0x7f, (byte) 0x7f,
                        (byte) 0x7f, (byte) 0x7f, (byte) 0x7f,
                        (byte) 0x7f, (byte) 0x7f, (byte) 0x7f,
                        (byte) 0x7f, (byte) 0x7f, (byte) 0x7f,
                        (byte) 0x00, (byte) 0x5F, (byte) 0x00,
                        (byte) 0x00, (byte) 0x5F, (byte) 0x00,
                        (byte) 0x00, (byte) 0x5F, (byte) 0x00,
                        (byte) 0x00, (byte) 0x5F, (byte) 0x00,
                        (byte) 0x00, (byte) 0x5F, (byte) 0x00,
                        (byte) 0x00, (byte) 0x5F, (byte) 0x00,
                        (byte) 0x00, (byte) 0x5F, (byte) 0x00,
                        (byte) 0x00, (byte) 0x5F, (byte) 0x00,
                };
                setLeds(data);
            }
        });

        btnWalkMid.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                byte[] data = new byte[]{
                        (byte) 0x3F, (byte) 0x3F, (byte) 0x3F,
                        (byte) 0x3F, (byte) 0x3F, (byte) 0x3F,
                        (byte) 0x3F, (byte) 0x3F, (byte) 0x3F,
                        (byte) 0x3F, (byte) 0x3F, (byte) 0x3F,
                        (byte) 0x3F, (byte) 0x3F, (byte) 0x3F,
                        (byte) 0x3F, (byte) 0x3F, (byte) 0x3F,
                        (byte) 0x3F, (byte) 0x3F, (byte) 0x3F,
                        (byte) 0x3F, (byte) 0x3F, (byte) 0x3F,
                        (byte) 0x3F, (byte) 0x3F, (byte) 0x3F,
                        (byte) 0x00, (byte) 0x1F, (byte) 0x00,
                        (byte) 0x00, (byte) 0x1F, (byte) 0x00,
                        (byte) 0x00, (byte) 0x1F, (byte) 0x00,
                        (byte) 0x00, (byte) 0x1F, (byte) 0x00,
                        (byte) 0x00, (byte) 0x1F, (byte) 0x00,
                        (byte) 0x00, (byte) 0x1F, (byte) 0x00,
                        (byte) 0x00, (byte) 0x1F, (byte) 0x00,
                        (byte) 0x00, (byte) 0x1F, (byte) 0x00,
                        (byte) 0x00, (byte) 0x1F, (byte) 0x00,
                };
                setLeds(data);
            }
        });


        btnWalkDim.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                byte[] data = new byte[]{
                        (byte) 0x08, (byte) 0x08, (byte) 0x08,
                        (byte) 0x08, (byte) 0x08, (byte) 0x08,
                        (byte) 0x08, (byte) 0x08, (byte) 0x08,
                        (byte) 0x08, (byte) 0x08, (byte) 0x08,
                        (byte) 0x08, (byte) 0x08, (byte) 0x08,
                        (byte) 0x08, (byte) 0x08, (byte) 0x08,
                        (byte) 0x08, (byte) 0x08, (byte) 0x08,
                        (byte) 0x08, (byte) 0x08, (byte) 0x08,
                        (byte) 0x08, (byte) 0x08, (byte) 0x08,
                        (byte) 0x08, (byte) 0x08, (byte) 0x08,
                        (byte) 0x00, (byte) 0x08, (byte) 0x00,
                        (byte) 0x00, (byte) 0x08, (byte) 0x00,
                        (byte) 0x00, (byte) 0x08, (byte) 0x00,
                        (byte) 0x00, (byte) 0x08, (byte) 0x00,
                        (byte) 0x00, (byte) 0x08, (byte) 0x00,
                        (byte) 0x00, (byte) 0x08, (byte) 0x00,
                        (byte) 0x00, (byte) 0x08, (byte) 0x00,
                        (byte) 0x00, (byte) 0x08, (byte) 0x00,
                };
                setLeds(data);
            }
        });


        btnSpdStop.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (m_gatt != null) {
                    UUID hatServiceUUID = UUID.fromString("65dbc53e-0000-4422-947c-f016c0e0af10");
                    BluetoothGattService hatService = m_gatt.getService(hatServiceUUID);
                    UUID hatCharacteristicUUID = UUID.fromString("65dbc53e-0001-4422-947c-f016c0e0af10");
                    BluetoothGattCharacteristic hatCharacteristic = hatService.getCharacteristic(hatCharacteristicUUID);
                    UUID hatSpeedCharacteristicUUID = UUID.fromString("65dbc53e-0002-4422-947c-f016c0e0af10");
                    BluetoothGattCharacteristic hatSpeedCharacteristic = hatService.getCharacteristic(hatSpeedCharacteristicUUID);


                    ByteBuffer b = ByteBuffer.allocate(Short.SIZE / Byte.SIZE)
                            .order(ByteOrder.LITTLE_ENDIAN);
                    b.putShort((short) 0);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        m_gatt.writeCharacteristic(hatSpeedCharacteristic, b.array(),
                                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    } else {
                        // how to do this on older android versions??
                        hatSpeedCharacteristic.setValue(b.array());
                        hatSpeedCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                        m_gatt.writeCharacteristic(hatSpeedCharacteristic);
                    }

                }
            }
        });


        btnSpdSlow.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (m_gatt != null) {
                    UUID hatServiceUUID = UUID.fromString("65dbc53e-0000-4422-947c-f016c0e0af10");
                    BluetoothGattService hatService = m_gatt.getService(hatServiceUUID);
                    UUID hatCharacteristicUUID = UUID.fromString("65dbc53e-0001-4422-947c-f016c0e0af10");
                    BluetoothGattCharacteristic hatCharacteristic = hatService.getCharacteristic(hatCharacteristicUUID);
                    UUID hatSpeedCharacteristicUUID = UUID.fromString("65dbc53e-0002-4422-947c-f016c0e0af10");
                    BluetoothGattCharacteristic hatSpeedCharacteristic = hatService.getCharacteristic(hatSpeedCharacteristicUUID);


                    ByteBuffer b = ByteBuffer.allocate(Short.SIZE / Byte.SIZE)
                            .order(ByteOrder.LITTLE_ENDIAN);
                    b.putShort((short) 1000);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        m_gatt.writeCharacteristic(hatSpeedCharacteristic, b.array(),
                                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    } else {
                        // how to do this on older android versions??
                        hatSpeedCharacteristic.setValue(b.array());
                        hatSpeedCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                        m_gatt.writeCharacteristic(hatSpeedCharacteristic);
                    }

                }
            }
        });


        btnSpdMid.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (m_gatt != null) {
                    UUID hatServiceUUID = UUID.fromString("65dbc53e-0000-4422-947c-f016c0e0af10");
                    BluetoothGattService hatService = m_gatt.getService(hatServiceUUID);
                    UUID hatCharacteristicUUID = UUID.fromString("65dbc53e-0001-4422-947c-f016c0e0af10");
                    BluetoothGattCharacteristic hatCharacteristic = hatService.getCharacteristic(hatCharacteristicUUID);
                    UUID hatSpeedCharacteristicUUID = UUID.fromString("65dbc53e-0002-4422-947c-f016c0e0af10");
                    BluetoothGattCharacteristic hatSpeedCharacteristic = hatService.getCharacteristic(hatSpeedCharacteristicUUID);


                    ByteBuffer b = ByteBuffer.allocate(Short.SIZE / Byte.SIZE)
                            .order(ByteOrder.LITTLE_ENDIAN);
                    b.putShort((short) 250);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        m_gatt.writeCharacteristic(hatSpeedCharacteristic, b.array(),
                                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    } else {
                        // how to do this on older android versions??

                        hatSpeedCharacteristic.setValue(b.array());
                        hatSpeedCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                        m_gatt.writeCharacteristic(hatSpeedCharacteristic);
                    }

                }
            }
        });


        btnSpdFast.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (m_gatt != null) {
                    UUID hatServiceUUID = UUID.fromString("65dbc53e-0000-4422-947c-f016c0e0af10");
                    BluetoothGattService hatService = m_gatt.getService(hatServiceUUID);
                    UUID hatCharacteristicUUID = UUID.fromString("65dbc53e-0001-4422-947c-f016c0e0af10");
                    BluetoothGattCharacteristic hatCharacteristic = hatService.getCharacteristic(hatCharacteristicUUID);
                    UUID hatSpeedCharacteristicUUID = UUID.fromString("65dbc53e-0002-4422-947c-f016c0e0af10");
                    BluetoothGattCharacteristic hatSpeedCharacteristic = hatService.getCharacteristic(hatSpeedCharacteristicUUID);


                    ByteBuffer b = ByteBuffer.allocate(Short.SIZE / Byte.SIZE)
                            .order(ByteOrder.LITTLE_ENDIAN);
                    b.putShort((short) 75);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        m_gatt.writeCharacteristic(hatSpeedCharacteristic, b.array(),
                                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    } else {
                        // how to do this on older android versions??

                        hatSpeedCharacteristic.setValue(b.array());
                        hatSpeedCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                        m_gatt.writeCharacteristic(hatSpeedCharacteristic);
                    }
                }
            }
        });


        btnDirRight.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (m_gatt != null) {
                    UUID hatServiceUUID = UUID.fromString("65dbc53e-0000-4422-947c-f016c0e0af10");
                    BluetoothGattService hatService = m_gatt.getService(hatServiceUUID);
                    UUID hatDirectionCharacteristicUUID = UUID.fromString("65dbc53e-0003-4422-947c-f016c0e0af10");
                    BluetoothGattCharacteristic hatDirectionCharacteristic = hatService.getCharacteristic(hatDirectionCharacteristicUUID);

                    byte[] data = new byte[]{0};

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        m_gatt.writeCharacteristic(hatDirectionCharacteristic, data,
                                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    } else {
                        // how to do this on older android versions??

                        hatDirectionCharacteristic.setValue(data);
                        hatDirectionCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                        m_gatt.writeCharacteristic(hatDirectionCharacteristic);
                    }

                }
            }
        });

        btnDirLeft.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (m_gatt != null) {
                    UUID hatServiceUUID = UUID.fromString("65dbc53e-0000-4422-947c-f016c0e0af10");
                    BluetoothGattService hatService = m_gatt.getService(hatServiceUUID);
                    UUID hatDirectionCharacteristicUUID = UUID.fromString("65dbc53e-0003-4422-947c-f016c0e0af10");
                    BluetoothGattCharacteristic hatDirectionCharacteristic = hatService.getCharacteristic(hatDirectionCharacteristicUUID);

                    byte[] data = new byte[]{1};

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        m_gatt.writeCharacteristic(hatDirectionCharacteristic, data,
                                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    } else {

                        hatDirectionCharacteristic.setValue(data);
                        hatDirectionCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        m_gatt.writeCharacteristic(hatDirectionCharacteristic);

                    }
                }
            }
        });

        lvHats = findViewById(R.id.lvHats);

        mCustomAdapter = new CustomAdapter(this);
        lvHats.setAdapter(mCustomAdapter);

        lvHats.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCustomAdapter.selectItem(i);
                mCustomAdapter.notifyDataSetChanged();
                connectButtonEnable(true);
            }
        });


        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "Device does not support Bluetooth", Toast.LENGTH_LONG);
            toast.show();
        } else {


            boolean permissionRequired = false;
            for (String permission : getRequiredPermissions()) {
                if ((ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)) {
                    // permission required
                    permissionRequired = true;
                }
            }

            setDisconnected();

            if (permissionRequired) {
                btnPermission.setEnabled(true);
                btnScan.setEnabled(false);
            } else {
                btnPermission.setEnabled(false);
                btnScan.setEnabled(true);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0xB7:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    btnPermission.setEnabled(false);
                    setDisconnected();
                } else {
                    btnPermission.setEnabled(true);
                    Toast toast = Toast.makeText(getApplicationContext(), "Permission required to use Bluetooth", Toast.LENGTH_LONG);
                    toast.show();
                }
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }

    @Override
    protected void onDestroy() {
        Log.i("hat", "onDestroy called");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Well... don't ask for permissions when we are quitting
        } else {
            if (m_gatt != null)
                m_gatt.disconnect();
        }
        super.onDestroy();

    }

    @SuppressLint("MissingPermission")
    void scanForDevices() {

        Toast toast = Toast.makeText(getApplicationContext(), "Scanning for hats", Toast.LENGTH_LONG);
        toast.show();

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                if (btScanner != null)
                    btScanner.stopScan(leScanCallback);
                Looper.prepare(); // whatever this does, it's required to toast in this thread.
                Toast toast = Toast.makeText(getApplicationContext(), "Stopping Scan", Toast.LENGTH_LONG);
                toast.show();

                scanButtonEnable(true);
            }
        };
        timer.schedule(timerTask, 10000);

        mCustomAdapter.clearItems();

        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        btScanner = bluetoothAdapter.getBluetoothLeScanner();
        btScanner.startScan(leScanCallback);
        scanButtonEnable(false);
        connectButtonEnable(false);

        //List<BluetoothDevice> connectedDevices = bluetoothManager.getConnectedDevices(GATT_SERVER);
        List<BluetoothDevice> connectedDevices = bluetoothManager.getConnectedDevices(GATT);
        for (int i = 0; i < connectedDevices.size(); i++) {
            BluetoothDevice device = connectedDevices.get(i);
            if (null != device) {
                device.fetchUuidsWithSdp();
                ParcelUuid hatUUID = ParcelUuid.fromString("65dbc53e-0000-4422-947c-f016c0e0af10");

                ParcelUuid[] uuids = device.getUuids();
                // it seems uuids is null here. Well... is there a way to get this for already
                // connected devices to be recognised at the moment I start?

                // Probably a custom connected, where I call connectGatt() (hopefully it will
                // succeed despite the fact I am already connected) and then perform discovery
                // and if it is the device I am looking for, call the regular connect procedure
                // on it. --- but this will interact with the device, wouldn't it?
                // Discovery is an active procedure. This means we will interact with all
                // connected bluetooth devices, including those that are not ours, just to
                // see they are not ours. Is this acceptable behaviour????

                if (uuids != null) {
                    for (ParcelUuid uuid : uuids) {
                        if (uuid.equals(hatUUID)) {
                            m_device = connectedDevices.get(i);
                            connect();
                        }
                    }
                }
            }
        }
    }

    void permissionButtonEnable(boolean enabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button btnPermission = findViewById(R.id.btnPermission);
                btnPermission.setEnabled(enabled);
            }
        });
    }

    void scanButtonEnable(boolean enabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button btnScan = findViewById(R.id.btnScan);
                btnScan.setEnabled(enabled);
            }
        });
    }

    void connectButtonEnable(boolean enabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnConnect = findViewById(R.id.btnConnect);
                btnConnect.setEnabled(enabled);
            }
        });
    }

    void setBattertVoltage(int voltage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView txtBatVolt = findViewById(R.id.txtBatVolt);
                txtBatVolt.setText(String.format("Battery: %6d mV", voltage));
            }
        });
    }


    void askForPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, getRequiredPermissions(), 0xB7);
    }

    String[] getRequiredPermissions() {
        // Different android versions require a different set of permissions to use bluetooth
        // This function lists the appropriate permissions for the version we are running on.

        ArrayList<String> result = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // "Build.VERSION_CODES.S" means Android 12
            // See https://apilevels.com/
            // In Android 12 the permission model for bluetooth changed
            // Thus we need different permission depending on the version.
            result.add("android.permission.BLUETOOTH_SCAN");
            result.add("android.permission.BLUETOOTH_CONNECT");
        } else {
            result.add("android.permission.BLUETOOTH");
            result.add("android.permission.BLUETOOTH_ADMIN");
            result.add("android.permission.ACCESS_FINE_LOCATION"); // required for 10
            result.add("android.permission.ACCESS_COARSE_LOCATION");

            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
                // "Build.VERSION_CODES.R" is Android 11.
                // This Android version required an additional
                // permission according to
                // https://medium.com/@elementalistbtg/android-permissions-for-bluetooth-1f3683ec6f5
                // I do not have an Android 11 device and the emulator
                // does not emulate Bluetooth. Therefore this remains untested.
                result.add("android.permission.ACCESS_BACKGROUND_LOCATION");
            }
        }
        return result.toArray(new String[0]);
    }


    void setConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnPermission.setEnabled(false);
                btnScan.setEnabled(false);
                btnConnect.setEnabled(false);
                btnDisconnect.setEnabled(true);

                btnRbDim.setEnabled(true);
                btnRbMid.setEnabled(true);
                btnRbBright.setEnabled(true);

                btnCfDim.setEnabled(true);
                btnCfMid.setEnabled(true);
                btnCfBright.setEnabled(true);

                btnCvDim.setEnabled(true);
                btnCvMid.setEnabled(true);
                btnCvBright.setEnabled(true);

                btnSpdStop.setEnabled(true);
                btnSpdSlow.setEnabled(true);
                btnSpdMid.setEnabled(true);
                btnSpdFast.setEnabled(true);

                btnDirRight.setEnabled(true);
                btnDirLeft.setEnabled(true);

                btnWalkBright.setEnabled(true);
                btnWalkMid.setEnabled(true);
                btnWalkDim.setEnabled(true);

                mCustomAdapter.clearItems();
                mCustomAdapter.addItem(m_device);
                mCustomAdapter.setConnected(true);
            }
        });
    }

    void setDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                btnPermission.setEnabled(false);
                btnScan.setEnabled(true);
                btnConnect.setEnabled(false);
                btnDisconnect.setEnabled(false);

                btnRbDim.setEnabled(false);
                btnRbMid.setEnabled(false);
                btnRbBright.setEnabled(false);

                btnCfDim.setEnabled(false);
                btnCfMid.setEnabled(false);
                btnCfBright.setEnabled(false);

                btnCvDim.setEnabled(false);
                btnCvMid.setEnabled(false);
                btnCvBright.setEnabled(false);

                btnSpdStop.setEnabled(false);
                btnSpdSlow.setEnabled(false);
                btnSpdMid.setEnabled(false);
                btnSpdFast.setEnabled(false);

                btnDirRight.setEnabled(false);
                btnDirLeft.setEnabled(false);

                btnWalkBright.setEnabled(false);
                btnWalkMid.setEnabled(false);
                btnWalkDim.setEnabled(false);

                mCustomAdapter.clearItems();
                mCustomAdapter.setConnected(false);

            }
        });
    }

    void setLeds(byte[] data) {
        if (m_gatt != null) {
            UUID hatServiceUUID = UUID.fromString("65dbc53e-0000-4422-947c-f016c0e0af10");
            BluetoothGattService hatService = m_gatt.getService(hatServiceUUID);
            UUID hatCharacteristicUUID = UUID.fromString("65dbc53e-0001-4422-947c-f016c0e0af10");
            BluetoothGattCharacteristic hatCharacteristic = hatService.getCharacteristic(hatCharacteristicUUID);
            UUID hatSpeedCharacteristicUUID = UUID.fromString("65dbc53e-0002-4422-947c-f016c0e0af10");
            BluetoothGattCharacteristic hatSpeedCharacteristic = hatService.getCharacteristic(hatSpeedCharacteristicUUID);


            int position = 100;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                m_gatt.writeCharacteristic(hatCharacteristic, data,
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            } else {
                hatCharacteristic.setValue(data);
                hatCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                m_gatt.writeCharacteristic(hatCharacteristic);
            }
        }
    }

    @SuppressLint("MissingPermission")
    void connect() {
        m_device.connectGatt(getApplicationContext(),
                false,
                new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            // successfully connected to the GATT Server
                            Log.i("hat", "Connected");
                            gatt.discoverServices();
                            gatt.requestMtu(64);
                            m_gatt = gatt;
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            // disconnected from the GATT Server
                            Log.i("hat", "Disconnected");
                            m_gatt = null;
                            setDisconnected();
                        }
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            Log.i("hat", "Service discovered");

                            UUID hatServiceUUID = UUID.fromString("65dbc53e-0000-4422-947c-f016c0e0af10");
                            BluetoothGattService hatService = gatt.getService(hatServiceUUID);
                            UUID voltageUUID = UUID.fromString("65dbc53e-0100-4422-947c-f016c0e0af10");
                            BluetoothGattCharacteristic voltageCharacteristic = hatService.getCharacteristic(voltageUUID);

                            UUID BasServiceUUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
                            BluetoothGattService BasService = gatt.getService(BasServiceUUID);
                            UUID percentUUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
                            BluetoothGattCharacteristic percentCharacteristic = BasService.getCharacteristic(percentUUID);

                            // Do I really have to specify the CCC Descriptor UUID manuallu??
                            // I would expect it to be in android somewhere.
                            String CCC_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";

                            BluetoothGattDescriptor voltageDescriptor = voltageCharacteristic.getDescriptor(UUID.fromString(CCC_DESCRIPTOR_UUID));
                            gatt.setCharacteristicNotification(voltageDescriptor.getCharacteristic(), true);

                            BluetoothGattDescriptor percentDescriptor = percentCharacteristic.getDescriptor(UUID.fromString(CCC_DESCRIPTOR_UUID));
                            gatt.setCharacteristicNotification(percentDescriptor.getCharacteristic(), true);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                // Do we need to trigger the next message on a callback?
                                gatt.writeDescriptor(voltageDescriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(percentDescriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            } else {
                                voltageDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(voltageDescriptor);
                                percentDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(percentDescriptor);
                            }

                            setConnected();

                        } else {
                            Log.w("hat", "onServicesDiscovered failed: " + status);
                        }
                    }

                    @Override
                    public void onCharacteristicChanged(
                            BluetoothGatt gatt,
                            BluetoothGattCharacteristic characteristic
                    ) {
                        switch (characteristic.getUuid().toString()) {
                            case "65dbc53e-0100-4422-947c-f016c0e0af10":
                                //Log.i("hat", "Battery Voltage: "  + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,0).toString());
                                setBattertVoltage(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));
                                break;
                            case "00002a19-0000-1000-8000-00805f9b34fb":
                                Log.i("hat", "Battery Percentage: " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0).toString());
                                break;
                            default:
                                Log.i("hat", "Unrecognise UUID: " + characteristic.getUuid().toString());
                                break;
                        }
                    }
                }
        );
    }

}