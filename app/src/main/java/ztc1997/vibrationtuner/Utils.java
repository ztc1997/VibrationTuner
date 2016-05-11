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

public class Utils {
    public static int[] scaleIntArray(int[] origin, float scale) {
        int[] result = new int[origin.length];
        for (int i = 0; i < origin.length; i++) {
            result[i] = (int) (origin[i] * scale);
        }
        return result;
    }
    public static long[] scaleLongArray(long[] origin, float scale) {
        long[] result = new long[origin.length];
        for (int i = 0; i < origin.length; i++) {
            result[i] = (long) (origin[i] * scale);
        }
        return result;
    }

    public static long[] toLongArray(int[] origin) {
        long[] result = new long[origin.length];
        for (int i = 0; i < origin.length; i++) {
            result[i] = origin[i];
        }
        return result;
    }

    public static float percentageIntToFloat(int percentage) {
        return (float) percentage / 100;
    }
}
