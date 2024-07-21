package com.example.javatest;




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
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    Timer timer;
    TimerTask timerTask;
    BluetoothLeScanner btScanner;

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

        Button btnPermission = findViewById(R.id.btnPermission);
        Button btnScan = findViewById(R.id.btnScan);
        Button btnConnect = findViewById(R.id.btnConnect);


        Button btnRbLow = findViewById(R.id.btnRbDim);
        Button btnRbMid = findViewById(R.id.btnRbMid);
        Button btnRbBright = findViewById(R.id.btnRbBright);

        Button btnCfLow = findViewById(R.id.btnCfDim);
        Button btnCfMid = findViewById(R.id.btnCfMid);
        Button btnCfBright = findViewById(R.id.btnCfBright);

        Button btnCvLow = findViewById(R.id.btnCvDim);
        Button btnCvMid = findViewById(R.id.btnCvMid);
        Button btnCvBright = findViewById(R.id.btnCvBright);


        Button btnSpdStop = findViewById(R.id.btnSpdStop);
        Button btnSpdSlow = findViewById(R.id.btnSpdSlow);
        Button btnSpdMid = findViewById(R.id.btnSpdMid);
        Button btnSpdFast = findViewById(R.id.btnSpdFast);

        Button btnDirRight = findViewById(R.id.btnDirRight);
        Button btnDirLeft = findViewById(R.id.btnDirLeft);

        btnPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askForPermission();
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(getApplicationContext(), "Scanning for hats", Toast.LENGTH_LONG);
                toast.show();
                scanForDevices();
            }
        });


        btnConnect.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                m_device = mCustomAdapter.getSelectedDevice();
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
                                        setBattertVoltage(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,0));
                                        break;
                                    case "00002a19-0000-1000-8000-00805f9b34fb":
                                        Log.i("hat", "Battery Percentage: "  + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0).toString());
                                    break;
                                        default:
                                        Log.i("hat", "Unrecognise UUID: " + characteristic.getUuid().toString()) ;
                                        break;
                                }



                            }


                        }
                );
                connectButtonEnable(false);
            }
        });


        btnRbBright.setOnClickListener(new View.OnClickListener() {
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


                    int position = 100;
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

                    // Writing to a characteristic has changed in Android 13
                    // Android 13 == TIRAMISU == API Level 33
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
        });

        btnRbMid.setOnClickListener(new View.OnClickListener() {
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
        });


        btnRbLow.setOnClickListener(new View.OnClickListener() {
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


                    int position = 100;
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
        });


        btnCfBright.setOnClickListener(new View.OnClickListener() {
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


                    int position = 100;
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
        });

        btnCfMid.setOnClickListener(new View.OnClickListener() {
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
        });


        btnCfLow.setOnClickListener(new View.OnClickListener() {
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
        });


        btnCvBright.setOnClickListener(new View.OnClickListener() {
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


                    int position = 100;
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
        });


        btnCvMid.setOnClickListener(new View.OnClickListener() {
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


                    int position = 100;
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
        });


        btnCvLow.setOnClickListener(new View.OnClickListener() {
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


                    int position = 100;
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
                        // how to do this on older android versions??

                        hatDirectionCharacteristic.setValue(data);
                        hatDirectionCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        m_gatt.writeCharacteristic(hatDirectionCharacteristic);


                    }

                }
            }
        });

        ListView newDevicesListView = findViewById(R.id.lvHats);

        mCustomAdapter = new CustomAdapter(this);
        newDevicesListView.setAdapter(mCustomAdapter);


        newDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

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

            if (permissionRequired) {
                btnPermission.setEnabled(true);
                btnScan.setEnabled(false);
            } else {
                btnPermission.setEnabled(false);
                btnScan.setEnabled(true);
            }
            connectButtonEnable(false);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Button btnPermission = findViewById(R.id.btnPermission);
        Button btnScan = findViewById(R.id.btnScan);

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0xB7:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    btnPermission.setEnabled(false);
                    btnScan.setEnabled(true);
                } else {
                    btnPermission.setEnabled(true);
                    btnScan.setEnabled(false);

                    // Explain to the user that the feature is unavailable because
                    // the feature requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
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

    void scanForDevices() {

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
        timer.schedule(timerTask, 5000);

        mCustomAdapter.clearItems();


        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        btScanner = bluetoothAdapter.getBluetoothLeScanner();
        btScanner.startScan(leScanCallback);
        scanButtonEnable(false);
        connectButtonEnable(false);


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
                Button btnConnect = findViewById(R.id.btnConnect);
                btnConnect.setEnabled(enabled);
            }
        });
    }

    void setBattertVoltage(int voltage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView txtBatVolt = findViewById(R.id.txtBatVolt);
                txtBatVolt.setText(String.format("Battery: %6d mV",voltage));
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
            result.add("android.permission.ACCESS_FINE_LOCATION");
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
}