/*
 * Copyright 2015-2016 Alex Zhang aka. ztc1997
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ztc1997.vibrationtuner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;

import static ztc1997.vibrationtuner.Utils.percentageIntToFloat;

public class VibratorServiceHooks {
    private static final String VIBRATOR_SERVICE = "com.android.server.VibratorService";

    private static float mVibStrength;
    private static Context mContext;

    public static void doHook(ClassLoader loader) {
        XSharedPreferences preferences = new XSharedPreferences(BuildConfig.APPLICATION_ID);
        mVibStrength = percentageIntToFloat(preferences.getInt(SettingsActivity.PREF_GLOBAL_VIB_STRENGTH, 100));

        final Class<?> vbratorService = XposedHelpers.findClass(VIBRATOR_SERVICE, loader);

        XposedHelpers.findAndHookConstructor(vbratorService, Context.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                mContext = (Context) param.args[0];
                mContext.registerReceiver(prefReceiver, new IntentFilter(SettingsActivity.ACTION_PREF_CHANGED));
            }
        });

        XposedHelpers.findAndHookMethod(vbratorService, "vibratorOn", long.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                long milliseconds = (long) param.args[0];
                param.args[0] = (long) (milliseconds * mVibStrength);
            }
        });
    }

    private static BroadcastReceiver prefReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SettingsActivity.ACTION_PREF_CHANGED)) {
                if (intent.hasExtra(SettingsActivity.PREF_GLOBAL_VIB_STRENGTH))
                    mVibStrength = percentageIntToFloat(intent.getIntExtra(SettingsActivity.PREF_GLOBAL_VIB_STRENGTH, 100));
            }
        }
    };
}
