package com.ugopiemontese.openband;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.prefs.Preferences;

public class MiUserActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_user);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(R.id.content, new PrefsFragment()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_done:
                Intent graph = new Intent(getApplicationContext(), MiGraphActivity.class);
                startActivity(graph);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ValidFragment")
    public static class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        public PrefsFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.user_preferences);

            Preference name = findPreference(MiBandConstants.PREFERENCE_NAME);
            Preference height = findPreference(MiBandConstants.PREFERENCE_HEIGHT);
            Preference weight = findPreference(MiBandConstants.PREFERENCE_WEIGHT);
            Preference sex = findPreference(MiBandConstants.PREFERENCE_GENDER);
            Preference age = findPreference(MiBandConstants.PREFERENCE_AGE);

            name.setSummary(getResources().getString(R.string.summary_name_preference));
            height.setSummary(getResources().getString(R.string.summary_height_preference));
            weight.setSummary(getResources().getString(R.string.summary_weight_preference));
            sex.setSummary(getResources().getString(R.string.summary_sex_preference));
            age.setSummary(getResources().getString(R.string.summary_age_preference));

        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onDestroy() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onDestroy();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            pref.setSummary(sharedPreferences.getString(key, ""));
        }

    }

}