package com.example.javatest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    //List<String> subject_list; // for temporary list

    Timer timer;
    TimerTask timerTask;;
    BluetoothLeScanner btScanner;


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


        ListView newDevicesListView = findViewById(R.id.lvHats);


        mCustomAdapter = new CustomAdapter(this);
        newDevicesListView.setAdapter(mCustomAdapter);


        newDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCustomAdapter.selectItem(i);
                mCustomAdapter.notifyDataSetChanged();
            }
        });


        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "Device does not support Bluetooth", Toast.LENGTH_LONG);
            toast.show();
        } else {


            // TODO different android versions
            if ((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)) {


                btnPermission.setEnabled(true);
                btnScan.setEnabled(false);
            } else {
                btnPermission.setEnabled(false);
                btnScan.setEnabled(true);
            }
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

    void askForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.BLUETOOTH_SCAN", "android.permission.BLUETOOTH_CONNECT"}, 0xB7);
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN", "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_BACKGROUND_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}, 0xB7);
        }
    }
}