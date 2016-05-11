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
import android.content.res.Resources;
import android.content.res.XResources;
import android.os.Build;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;

import static ztc1997.vibrationtuner.Utils.percentageIntToFloat;
import static ztc1997.vibrationtuner.Utils.scaleLongArray;
import static ztc1997.vibrationtuner.Utils.toLongArray;

public class PhoneWindowManagerHooks {
    private static float mLongPressVibeStrength, mVirtualKeyVibeStrength, mKeyboardTapVibeStrength, mClockTickVibeStrength, mCalendarDateVibeStrength;
    public static long[] mLongPressVibePattern, mVirtualKeyVibePattern, mKeyboardTapVibePattern, mClockTickVibePattern, mCalendarDateVibePattern;
    private static Context mContext;
    private static Object mPhoneWindowManager;

    public static void doHook(ClassLoader classLoader) {
        XSharedPreferences preferences = new XSharedPreferences(BuildConfig.APPLICATION_ID);
        Resources res = XResources.getSystem();

        mLongPressVibeStrength = percentageIntToFloat(preferences.getInt(SettingsActivity.PREF_LONG_PRESS_VIB_STRENGTH, 100));
        mVirtualKeyVibeStrength = percentageIntToFloat(preferences.getInt(SettingsActivity.PREF_VIRTUAL_KEY_VIB_STRENGTH, 100));
        mKeyboardTapVibeStrength = percentageIntToFloat(preferences.getInt(SettingsActivity.PREF_KEYBOARD_TAP_VIB_STRENGTH, 100));
        mClockTickVibeStrength = percentageIntToFloat(preferences.getInt(SettingsActivity.PREF_CLOCK_TICK_VIB_STRENGTH, 100));
        mCalendarDateVibeStrength = percentageIntToFloat(preferences.getInt(SettingsActivity.PREF_CALENDAR_DATE_VIB_STRENGTH, 100));

        final int longPressVibeId = res.getIdentifier("config_longPressVibePattern", "array", XposedInit.ANDROID);
        final int virtualKeyVibeId = res.getIdentifier("config_virtualKeyVibePattern", "array", XposedInit.ANDROID);
        final int keyboardTapVibeId = res.getIdentifier("config_keyboardTapVibePattern", "array", XposedInit.ANDROID);
        final int clockTickVibeId = res.getIdentifier("config_clockTickVibePattern", "array", XposedInit.ANDROID);
        final int calendarDateVibeId = res.getIdentifier("config_calendarDateVibePattern", "array", XposedInit.ANDROID);

        mLongPressVibePattern = toLongArray(res.getIntArray(longPressVibeId));
        mVirtualKeyVibePattern = toLongArray(res.getIntArray(virtualKeyVibeId));
        mKeyboardTapVibePattern = toLongArray(res.getIntArray(keyboardTapVibeId));
        mClockTickVibePattern = toLongArray(res.getIntArray(clockTickVibeId));
        mCalendarDateVibePattern = toLongArray(res.getIntArray(calendarDateVibeId));

        final String CLASS_PHONE_WINDOW_MANAGER = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ?
                "com.android.internal.policy.impl.PhoneWindowManager" :
                "com.android.server.policy.PhoneWindowManager";
        final String CLASS_IWINDOW_MANAGER = "android.view.IWindowManager";
        final String CLASS_WINDOW_MANAGER_FUNCS = "android.view.WindowManagerPolicy.WindowManagerFuncs";

        XposedHelpers.findAndHookMethod(CLASS_PHONE_WINDOW_MANAGER, classLoader, "init",
                Context.class, CLASS_IWINDOW_MANAGER, CLASS_WINDOW_MANAGER_FUNCS, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        mPhoneWindowManager = param.thisObject;
                        mContext = (Context) param.args[0];
                        mContext.registerReceiver(prefReceiver, new IntentFilter(SettingsActivity.ACTION_PREF_CHANGED));
                        XposedHelpers.setObjectField(mPhoneWindowManager, "mLongPressVibePattern", scaleLongArray(mLongPressVibePattern, mLongPressVibeStrength));
                        XposedHelpers.setObjectField(mPhoneWindowManager, "mVirtualKeyVibePattern", scaleLongArray(mVirtualKeyVibePattern, mVirtualKeyVibeStrength));
                        XposedHelpers.setObjectField(mPhoneWindowManager, "mKeyboardTapVibePattern", scaleLongArray(mKeyboardTapVibePattern, mKeyboardTapVibeStrength));
                        XposedHelpers.setObjectField(mPhoneWindowManager, "mClockTickVibePattern", scaleLongArray(mClockTickVibePattern, mClockTickVibeStrength));
                        XposedHelpers.setObjectField(mPhoneWindowManager, "mCalendarDateVibePattern", scaleLongArray(mCalendarDateVibePattern, mCalendarDateVibeStrength));
                    }
                });
    }

    private static BroadcastReceiver prefReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SettingsActivity.ACTION_PREF_CHANGED)) {
                if (intent.hasExtra(SettingsActivity.PREF_LONG_PRESS_VIB_STRENGTH)) {
                    mVirtualKeyVibeStrength = percentageIntToFloat(intent.getIntExtra(SettingsActivity.PREF_LONG_PRESS_VIB_STRENGTH, 100));
                    XposedHelpers.setObjectField(mPhoneWindowManager, "mLongPressVibePattern", scaleLongArray(mLongPressVibePattern, mLongPressVibeStrength));
                }
                if (intent.hasExtra(SettingsActivity.PREF_VIRTUAL_KEY_VIB_STRENGTH)) {
                    mVirtualKeyVibeStrength = percentageIntToFloat(intent.getIntExtra(SettingsActivity.PREF_VIRTUAL_KEY_VIB_STRENGTH, 100));
                    XposedHelpers.setObjectField(mPhoneWindowManager, "mVirtualKeyVibePattern", scaleLongArray(mVirtualKeyVibePattern, mVirtualKeyVibeStrength));
                }
                if (intent.hasExtra(SettingsActivity.PREF_KEYBOARD_TAP_VIB_STRENGTH)) {
                    mVirtualKeyVibeStrength = percentageIntToFloat(intent.getIntExtra(SettingsActivity.PREF_KEYBOARD_TAP_VIB_STRENGTH, 100));
                    XposedHelpers.setObjectField(mPhoneWindowManager, "mKeyboardTapVibePattern", scaleLongArray(mKeyboardTapVibePattern, mKeyboardTapVibeStrength));
                }
                if (intent.hasExtra(SettingsActivity.PREF_CLOCK_TICK_VIB_STRENGTH)) {
                    mVirtualKeyVibeStrength = percentageIntToFloat(intent.getIntExtra(SettingsActivity.PREF_CLOCK_TICK_VIB_STRENGTH, 100));
                    XposedHelpers.setObjectField(mPhoneWindowManager, "mClockTickVibePattern", scaleLongArray(mClockTickVibePattern, mClockTickVibeStrength));
                }
                if (intent.hasExtra(SettingsActivity.PREF_CALENDAR_DATE_VIB_STRENGTH)) {
                    mVirtualKeyVibeStrength = percentageIntToFloat(intent.getIntExtra(SettingsActivity.PREF_CALENDAR_DATE_VIB_STRENGTH, 100));
                    XposedHelpers.setObjectField(mPhoneWindowManager, "mCalendarDateVibePattern", scaleLongArray(mCalendarDateVibePattern, mCalendarDateVibeStrength));
                }
            }
        }
    };
}
