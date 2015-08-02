/*
 * Copyright 2015. Alex Zhang aka. ztc1997
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ztc1997.vibrationtuner;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import de.psdev.licensesdialog.LicensesDialog;

public class SettingsActivity extends AppCompatActivity {
    public static final String ACTION_PREF_CHANGED = "ztc1997.vibrationtuner.SettingsActivity.action.ACTION_PREF_CHANGED";
    public static final String PREF_GLOBAL_VIB_STRENGTH = "pref_global_vib_strength";
    public static final String PREF_LONG_PRESS_VIB_STRENGTH = "pref_long_press_vib_strength";
    public static final String PREF_VIRTUAL_KEY_VIB_STRENGTH = "pref_virtual_key_vib_strength";
    public static final String PREF_CLOCK_TICK_VIB_STRENGTH = "pref_clock_tick_vib_strength";
    public static final String PREF_CALENDAR_DATE_VIB_STRENGTH = "pref_calendar_date_vib_strength";
    public static final String PREF_KEYBOARD_TAP_VIB_STRENGTH = "pref_keyboard_tap_vib_strength";
    private static final String ACTIVITY_ALIAS_NAME = SettingsActivity.class.getName() + "-LAUNCHER";

    private PackageManager mPackageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPackageManager = getPackageManager();

        checkActivated();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem showItem = menu.findItem(R.id.action_show_icon);
        MenuItem hideItem = menu.findItem(R.id.action_hide_icon);

        switch (mPackageManager.getComponentEnabledSetting(new ComponentName(this, ACTIVITY_ALIAS_NAME))) {
            case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
            case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:
                showItem.setVisible(false);
                hideItem.setVisible(true);
                break;
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
                showItem.setVisible(true);
                hideItem.setVisible(false);
                break;
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_notices:
                new LicensesDialog.Builder(this).setNotices(R.raw.notices).setIncludeOwnLicense(true).build().show();
                return true;
            case R.id.action_show_icon:
                mPackageManager.setComponentEnabledSetting(
                        new ComponentName(this, ACTIVITY_ALIAS_NAME),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
                return true;
            case R.id.action_hide_icon:
                mPackageManager.setComponentEnabledSetting(
                        new ComponentName(this, ACTIVITY_ALIAS_NAME),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkActivated() {
        if (activatedModuleVersion() != BuildConfig.VERSION_CODE) {
            new AlertDialog.Builder(this)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setMessage(R.string.msg_module_no_activated)
                    .setNegativeButton(android.R.string.ok, null)
                    .show();
        }
    }

    private int activatedModuleVersion() {
        return -1;
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private SharedPreferences preferences;

        @SuppressWarnings("deprecation")
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.settings);
            preferences = getPreferenceScreen().getSharedPreferences();
        }

        @Override
        public void onResume() {
            super.onResume();
            preferences.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            preferences.unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Intent intent = new Intent(ACTION_PREF_CHANGED);
            switch (key) {
                case PREF_GLOBAL_VIB_STRENGTH:
                case PREF_CALENDAR_DATE_VIB_STRENGTH:
                case PREF_CLOCK_TICK_VIB_STRENGTH:
                case PREF_LONG_PRESS_VIB_STRENGTH:
                case PREF_VIRTUAL_KEY_VIB_STRENGTH:
                case PREF_KEYBOARD_TAP_VIB_STRENGTH:
                    intent.putExtra(key, sharedPreferences.getInt(key, 100));
                    break;
            }
            getActivity().sendBroadcast(intent);
        }
    }
}
