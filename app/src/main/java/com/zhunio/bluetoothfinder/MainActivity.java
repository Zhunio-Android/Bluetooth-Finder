package com.zhunio.bluetoothfinder;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.R.layout;
import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
import static android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE;
import static android.bluetooth.BluetoothAdapter.getDefaultAdapter;
import static android.bluetooth.BluetoothDevice.ACTION_FOUND;
import static android.content.pm.PackageManager.PERMISSION_DENIED;

public class MainActivity extends AppCompatActivity {

    /** UI Search Button */
    private Button buttonSearch;

    /** Bluetooth interface provided by Android SDK */
    private BluetoothAdapter mBluetoothAdapter;

    /** Interfaces between the list view and the model */
    private ArrayAdapter<String> mArrayAdapter;

    /** List of bluetooth devices */
    private ArrayList<String> mBluetoothDevices;

    /** Broadcast Bluetooth signal to discover new Bluetooth devices */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        /**
         * Once a new Bluetooth device is discovered, onReceive is called and the device can be
         * processed.
         * @param context Application Context.
         * @param intent Intent.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                addDevice(device);
            } else if (ACTION_DISCOVERY_FINISHED.equals(action)) {
                toggleSearchButton(true, "Search");
            }
        }
    };

    /**
     * When the search button is clicked, the button is disabled and and the Bluetooth Adapter is
     * used to discover new devices.
     *
     * @param view the button view clicked.
     */
    public void onSearch(View view) {
        toggleSearchButton(false, "Searching");
        mBluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        buttonSearch = findViewById(R.id.button_search);
        ListView mListViewBluetoothDevices = findViewById(R.id.list_bluetooth_devices);

        // Initialize bluetooth adapter
        mBluetoothAdapter = getDefaultAdapter();

        // Setup list view
        mBluetoothDevices = new ArrayList<>();
        mArrayAdapter = new ArrayAdapter<>(this, layout.simple_list_item_1, mBluetoothDevices);
        mListViewBluetoothDevices.setAdapter(mArrayAdapter);

        // Ensure bluetooth is enabled
        ensureBluetoothIsEnable();

        // Request location permission on API Level >= 26
        requestAccessFineLocation();

        // Register BroadcastReceiver
        registerBroadcastReceiver(new String[]{ACTION_FOUND, ACTION_DISCOVERY_FINISHED});
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    /**
     * Adds a new Bluetooth device to the model and notifies the List View adapter of the data
     * set changed.
     *
     * @param device Bluetooth device to add to the model.
     */
    private void addDevice(BluetoothDevice device) {
        String deviceName = device.getName();
        String deviceMacAddress = device.getAddress();

        StringBuilder newDeviceString = new StringBuilder();

        if (deviceName != null)
            newDeviceString.append(deviceName).append(" ");
        if (deviceMacAddress != null)
            newDeviceString.append(deviceMacAddress);

        if (!mBluetoothDevices.contains(newDeviceString.toString())) {
            mBluetoothDevices.add(newDeviceString.toString());
            mArrayAdapter.notifyDataSetChanged();
        }

    }

    /**
     * Enables or disables the search button while simultaneously changing the text.
     *
     * @param enabled if the search button should be enabled.
     * @param text    text to set the button to.
     */
    private void toggleSearchButton(boolean enabled, CharSequence text) {
        buttonSearch.setEnabled(enabled);
        buttonSearch.setText(text);
    }

    /**
     * Register the Broadcast Receiver with the following actions.
     *
     * @param actions actions to add to the Broadcast Receiver
     */
    private void registerBroadcastReceiver(@NonNull String[] actions) {
        IntentFilter intentFilter = new IntentFilter();

        for (String action : actions)
            intentFilter.addAction(action);

        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    /**
     * Ensure Bluetooth is enabled.
     */
    private void ensureBluetoothIsEnable() {
        final int REQUEST_ENABLE_BLUETOOTH = 1;

        if (!mBluetoothAdapter.isEnabled())
            startActivityForResult(new Intent(ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BLUETOOTH);
    }

    /**
     * Request Access Fine Location permission.
     */
    private void requestAccessFineLocation() {
        final int REQUEST_ACCESS_FINE_LOCATION = 1;

        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_DENIED) {
            // ACCESS_FINE_LOCATION Permission is not granted
            // Request the permission
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION},
                    REQUEST_ACCESS_FINE_LOCATION);
        }
    }
}
