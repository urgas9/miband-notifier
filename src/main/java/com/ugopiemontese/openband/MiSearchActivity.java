package com.ugopiemontese.openband;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.ugopiemontese.openband.helper.MiBand;
import com.ugopiemontese.openband.helper.MiBandAdapter;

import java.util.LinkedList;
import java.util.List;

public class MiSearchActivity extends Activity implements AdapterView.OnItemClickListener {

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private RelativeLayout mSearchLayout;
    private TextView mTextView;
    private RelativeLayout mListLayout;
    private ListView mListView;
    private List mMiBandList;

    private static final long SCAN_PERIOD = 20000; // 10 seconds.

    private SharedPreferences sharedPreferences;
    private String mAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_search);

        mSearchLayout = (RelativeLayout) findViewById(R.id.search_parent);
        mTextView = (TextView) findViewById(R.id.text_search);
        mListLayout = (RelativeLayout) findViewById(R.id.list_parent);
        mListView = (ListView) findViewById(R.id.list);

        mMiBandList = new LinkedList();

        mHandler = new Handler();

        MiBandAdapter adapter = new MiBandAdapter(getApplicationContext(), R.layout.single_miband_element, mMiBandList);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(this);

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mAddress = sharedPreferences.getString(MiBandConstants.PREFERENCE_MAC_ADDRESS, "");

        if (mAddress.length() > 0 && (mAddress.startsWith(MiBandConstants.MAC_ADDRESS_FILTER) || mAddress.startsWith(MiBandConstants.MAC_ADDRESS_FILTER_1S))) {
            Intent overview = new Intent(getApplicationContext(), MiGraphActivity.class);
            startActivity(overview);
        } else {

            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                SnackbarManager.show(
                    Snackbar.with(getApplicationContext())
                        .text(getResources().getString(R.string.ble_not_supported))
                        .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                        .animation(true), this);
            } else {

                if (!mBluetoothAdapter.isEnabled()) {
                    SnackbarManager.show(
                        Snackbar.with(getApplicationContext())
                            .text(getResources().getString(R.string.enable_ble))
                            .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                            .animation(true)
                            .actionLabel(getResources().getString(R.string.action_enable_ble))
                            .actionColor(getResources().getColor(R.color.graph_color_primary))
                            .actionListener(new ActionClickListener() {
                                @Override
                                public void onActionClicked(Snackbar snackbar) {
                                Intent intentBluetooth = new Intent();
                                intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                                startActivity(intentBluetooth);
                                }
                            }), this);
                } else {
                    scanLeDevice(true);
                }

            }

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter.isEnabled() && mScanning)
            scanLeDevice(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothAdapter.isEnabled() && mScanning)
            scanLeDevice(false);
    }

    private void scanLeDevice(boolean enable) {
        if (enable) {

            mMiBandList.clear();
            mListView.invalidate();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanLeDevice(false);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
            String s = "!";

        } else {

            mScanning = false;
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);

            if (mMiBandList.size() > 0) {
                getActionBar().setTitle(R.string.choose_miband);
                mListLayout.setVisibility(View.VISIBLE);
                mSearchLayout.setVisibility(View.GONE);
            } else {
                getActionBar().setTitle(R.string.app_name);
                mListLayout.setVisibility(View.GONE);
                mSearchLayout.setVisibility(View.VISIBLE);
                mTextView.setText(getResources().getString(R.string.not_found));
            }
            showAlert();

        }
    }

    public void showAlert() {
        if (mBluetoothAdapter.isEnabled())
            SnackbarManager.show(
                Snackbar.with(getApplicationContext())
                    .text(getResources().getString(R.string.scan_again))
                    .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                    .animation(true)
                    .actionLabel(getResources().getString(R.string.action_scan_again))
                    .actionColor(getResources().getColor(R.color.graph_color_primary))
                    .actionListener(new ActionClickListener() {
                        @Override
                        public void onActionClicked(Snackbar snackbar) {
                        getActionBar().setTitle(R.string.app_name);
                        mTextView.setText(R.string.looking_for_miband);
                        mListLayout.setVisibility(View.GONE);
                        mSearchLayout.setVisibility(View.VISIBLE);
                        scanLeDevice(true);
                        }
                    }), this);
    }

    private ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result.getDevice() != null && isDeviceMiBand(result.getDevice())) {

                mMiBandList.add(new MiBand(result.getDevice().getName(), result.getDevice().getAddress()));
                mListView.invalidate();

            }
        }
    };

    private boolean isDeviceMiBand(BluetoothDevice device){
        return (device.getName().equals("MI1S") && device.getAddress().startsWith(MiBandConstants.MAC_ADDRESS_FILTER_1S) ||
                device.getName().equals("MI") && device.getAddress().startsWith(MiBandConstants.MAC_ADDRESS_FILTER));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        MiBand mMiBand = (MiBand) mMiBandList.get(position);

        editor.putString(MiBandConstants.PREFERENCE_MAC_ADDRESS, (String) mMiBand.getAddress());
        editor.commit();

        Intent user = new Intent(getApplicationContext(), MiUserActivity.class);
        startActivity(user);

    }

}