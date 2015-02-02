package com.ugopiemontese.openband;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;

public class PreferencesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_preferences);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(R.id.content, new PrefsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ValidFragment")
    public class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        public PrefsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();

            Preference name = findPreference(MiBandConstants.PREFERENCE_NAME);
            Preference height = findPreference(MiBandConstants.PREFERENCE_HEIGHT);
            Preference weight = findPreference(MiBandConstants.PREFERENCE_WEIGHT);
            Preference sex = findPreference(MiBandConstants.PREFERENCE_GENDER);
            Preference age = findPreference(MiBandConstants.PREFERENCE_AGE);
            Preference goal = findPreference(MiBandConstants.PREFERENCE_GOAL);
            Preference mac_address = findPreference(MiBandConstants.PREFERENCE_MAC_ADDRESS);
            Preference firmware = findPreference(MiBandConstants.PREFERENCE_FIRMWARE);

            name.setSummary(sharedPreferences.getString(MiBandConstants.PREFERENCE_NAME, getResources().getString(R.string.summary_name_preference)));
            height.setSummary(sharedPreferences.getString(MiBandConstants.PREFERENCE_HEIGHT, getResources().getString(R.string.summary_height_preference)));
            weight.setSummary(sharedPreferences.getString(MiBandConstants.PREFERENCE_WEIGHT, getResources().getString(R.string.summary_weight_preference)));
            sex.setSummary(sharedPreferences.getString(MiBandConstants.PREFERENCE_GENDER, getResources().getString(R.string.summary_sex_preference)));
            age.setSummary(sharedPreferences.getString(MiBandConstants.PREFERENCE_AGE, getResources().getString(R.string.summary_age_preference)));
            goal.setSummary(sharedPreferences.getString(MiBandConstants.PREFERENCE_GOAL, getResources().getString(R.string.summary_goal_preference)));
            mac_address.setSummary(sharedPreferences.getString(MiBandConstants.PREFERENCE_MAC_ADDRESS, ""));
            firmware.setSummary(sharedPreferences.getString(MiBandConstants.PREFERENCE_FIRMWARE, ""));

        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            pref.setSummary(sharedPreferences.getString(key, ""));
            if (key.equals(MiBandConstants.PREFERENCE_GOAL)) {
                Intent intent = new Intent("goal");
                intent.putExtra("goal", Integer.parseInt(sharedPreferences.getString(key, "5000")));
                LocalBroadcastManager.getInstance(PreferencesActivity.this).sendBroadcast(intent);
            }
        }

    }

}