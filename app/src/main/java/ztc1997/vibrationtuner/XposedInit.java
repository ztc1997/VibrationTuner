/*
 * Copyright 2015 Alex Zhang aka. ztc1997
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

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedInit implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    public static final String TAG = XposedInit.class.getSimpleName() + ": ";
    public static final String ANDROID = "android";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        switch (loadPackageParam.packageName) {
            case ANDROID:
                VibratorServiceHooks.doHook(loadPackageParam.classLoader);
                break;

            case BuildConfig.APPLICATION_ID:
                XposedHelpers.findAndHookMethod(SettingsActivity.class.getName(), loadPackageParam.classLoader,
                        "activatedModuleVersion", XC_MethodReplacement.returnConstant(BuildConfig.VERSION_CODE));
                break;
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        PhoneWindowManagerHooks.doHook();
    }
}
