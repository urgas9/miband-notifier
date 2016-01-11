package com.ugopiemontese.openband;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.ugopiemontese.openband.helper.Battery;
import com.ugopiemontese.openband.helper.MiBand;
import com.ugopiemontese.openband.helper.SwipeTouchListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

public class MiGraphActivity extends Activity {

    // BLUETOOTH
    private String mDeviceAddress;

    // UI
    private RelativeLayout mLoading;
    private LinearLayout mHolder;
    private PieChart mChart;
    private TextView mTVBatteryLevel;

    private int mSteps;
    private double mDistance;
    private float MIBAND_GOAL;
    private SharedPreferences sharedPreferences;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothMi;
    private BluetoothGatt mGatt;

    private MiBand mMiBand;

    private AsyncTask<Void, Void, Boolean> LoadDataAndRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_graph);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mDeviceAddress = sharedPreferences.getString(MiBandConstants.PREFERENCE_MAC_ADDRESS, "");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothMi = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
        mMiBand = new MiBand(mBluetoothMi.getName(), mDeviceAddress);

        MIBAND_GOAL = Float.parseFloat(sharedPreferences.getString(MiBandConstants.PREFERENCE_GOAL, "5000"));

        mTVBatteryLevel = (TextView) findViewById(R.id.text_battery_level);

        mHolder = (LinearLayout) findViewById(R.id.textHolder);
        mHolder.setVisibility(View.GONE);
        mLoading = (RelativeLayout) findViewById(R.id.loading);
        mLoading.setVisibility(View.VISIBLE);

        mChart = (PieChart) findViewById(R.id.piechart);
        mChart.setTouchEnabled(true);
        mChart.setRotationEnabled(false);
        mChart.setDrawLegend(false);
        mChart.setDrawXValues(false);
        mChart.setDrawYValues(false);
        mChart.highlightValues(null);
        mChart.setDescription("");
        mChart.setHoleColor(getResources().getColor(R.color.background_material_light));

        Paint paint = mChart.getPaint(Chart.PAINT_CENTER_TEXT);
        paint.setColor(getResources().getColor(R.color.graph_color_primary));

        mChart.setCenterTextSize(30f);
        mChart.setCenterTextTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

        mChart.setOnTouchListener(new SwipeTouchListener(this) {

            @Override
            public void onSwipeLeft() {
                int HEIGHT = Integer.valueOf(sharedPreferences.getString(MiBandConstants.PREFERENCE_HEIGHT, "175"));
                String GENDER = sharedPreferences.getString(MiBandConstants.PREFERENCE_GENDER, "Male");
                if (GENDER.equals("Male"))
                    mDistance = mSteps * HEIGHT * 0.415 / 100000;
                else
                    mDistance = mSteps * HEIGHT * 0.413 / 100000;
                mChart.setCenterText(String.format(Locale.getDefault(), "%.2f", mDistance) + "\n" + getResources().getString(R.string.distance));
                mChart.invalidate();
            }

            @Override
            public void onSwipeRight() {
                mChart.setCenterText(mSteps + "\n" + getResources().getString(R.string.steps));
                mChart.invalidate();
            }

            @Override
            public void onSwipeDown() {
                LoadDataAndRefresh = new LoadDataAndRefresh().execute();
            }

        });

    }

    @Override
    public void onResume() {
        super.onResume();
        LoadDataAndRefresh = new LoadDataAndRefresh().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(LoadDataAndRefresh != null && LoadDataAndRefresh.getStatus() != AsyncTask.Status.FINISHED) {
            LoadDataAndRefresh.cancel(true);
        }
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
            mGatt = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_graph, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_notifications:
                intent = new Intent(getApplicationContext(), MiOverviewActivity.class);
                startActivity(intent);
                break;
            case R.id.action_preferences:
                intent = new Intent(getApplicationContext(), PreferencesActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    public class LoadDataAndRefresh extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            mGatt = mBluetoothMi.connectGatt(getBaseContext(), false, mGattCallback);
            mGatt.connect();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                Thread.sleep(2000, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (mSteps != mMiBand.getSteps()) {
                mSteps = mMiBand.getSteps();
                ArrayList<Entry> yVals = new ArrayList<Entry>();
                yVals.add(new Entry(mSteps, 0));
                MIBAND_GOAL = Float.parseFloat(sharedPreferences.getString(MiBandConstants.PREFERENCE_GOAL, "5000"));
                yVals.add(new Entry((mSteps > MIBAND_GOAL) ? 0 : MIBAND_GOAL - mSteps, 1));
                PieDataSet set = new PieDataSet(yVals, "Steps");
                set.setSliceSpace(1f);
                ArrayList<Integer> colors = new ArrayList<Integer>();
                colors.add(getResources().getColor(R.color.graph_color_primary));
                colors.add(getResources().getColor(android.R.color.transparent));
                set.setColors(colors);
                ArrayList<String> xVals = new ArrayList<String>();
                xVals.add(0, "steps");
                xVals.add(1, "goal");
                PieData pie = new PieData(xVals, set);
                mChart.setData(pie);
                mChart.setCenterText(mSteps + "\n" + getResources().getString(R.string.steps));
                return true;
            } else {
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (isCancelled()) {
                if (mGatt != null) {
                    mGatt.disconnect();
                    mGatt.close();
                    mGatt = null;
                }
                mLoading.setVisibility(View.GONE);
                mHolder.setVisibility(View.VISIBLE);
                return;
            }

            mLoading.setVisibility(View.GONE);
            mHolder.setVisibility(View.VISIBLE);
            if (mMiBand.getBattery() != null) {
                mTVBatteryLevel.setText(mMiBand.getBattery().mBatteryLevel + "%");
            }
            mChart.animateXY(750, 750);
            mChart.spin(750, 0, 270);
            mChart.invalidate();
        }
    }

    private void request(UUID what) {
        mGatt.readCharacteristic(getMiliService().getCharacteristic(what));
    }

    private BluetoothGattService getMiliService() {
        return mGatt.getService(MiBandConstants.UUID_SERVICE_MILI_SERVICE);
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        int state = 0;

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED)
                gatt.discoverServices();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                request(MiBandConstants.UUID_CHARACTERISTIC_REALTIME_STEPS);
                for(BluetoothGattService s : gatt.getServices()){
                    Log.d("SERVICE", s.getUuid().toString());
                }

            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            byte[] b = characteristic.getValue();

            if ((b.length > 0) && !String.valueOf(b).equals("")) {
                //Log.i(characteristic.getUuid().toString(), "state: " + state + " value:" + Arrays.toString(b));
                if (characteristic.getUuid().equals(MiBandConstants.UUID_CHARACTERISTIC_REALTIME_STEPS)) {
                    int steps = 0xff & b[0] | (0xff & b[1]) << 8;
                    Log.d("STEPS", steps + "");
                    mMiBand.setSteps(0xff & b[0] | (0xff & b[1]) << 8);
                } else if (characteristic.getUuid().equals(MiBandConstants.UUID_CHARACTERISTIC_BATTERY)) {
                    Battery battery = Battery.fromByte(b);
                    Log.d("BATTERY", battery.toString());
                    mMiBand.setBattery(battery);
                } else if (characteristic.getUuid().equals(MiBandConstants.UUID_CHARACTERISTIC_DEVICE_INFO)) {
                    byte[] version = Arrays.copyOfRange(b, b.length - 4, b.length);
                    mMiBand.setFirmware(version);
                    if(version!= null) {
                        Log.d("FIRMWARE", version.toString());
                    }
                    else{
                        Log.d("FIRMWARE", "null");
                    }
                    sharedPreferences.edit().putString(MiBandConstants.PREFERENCE_FIRMWARE, mMiBand.getFirmware()).commit();
                }
                else if(characteristic.getUuid().equals(MiBandConstants.UUID_CHARACTERISTIC_DEVICE_NAME)){
                    String s = b.toString();
                    Log.d("SENSOR", s + ": data " + b);
                }
                state++;
            }

            switch (state) {
                case 0:
                    request(MiBandConstants.UUID_CHARACTERISTIC_REALTIME_STEPS);
                    break;
                case 1:
                    request(MiBandConstants.UUID_CHARACTERISTIC_BATTERY);
                    break;
                case 2:
                    request(MiBandConstants.UUID_CHARACTERISTIC_DEVICE_INFO);
                    break;
                case 3:
                    request(MiBandConstants.UUID_CHARACTERISTIC_DEVICE_NAME);
                    break;
                case 4:
                    state = 0;
                    break;
            }
        }
    };

}